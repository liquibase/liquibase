package liquibase.resource;

import liquibase.Scope;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtil;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A @{link ResourceAccessor} implementation for files on the file system.
 * Will look for files in zip and jar files if they are added as root paths.
 */
public class FileSystemResourceAccessor extends AbstractResourceAccessor {

    //Set to avoid duplicates but LinkedHashSet to preserve order. Kept private to control access through get/set since we are an ExtensibleObject
    private LinkedHashSet<Path> rootPaths = new LinkedHashSet<>();

    /**
     * Creates a FileSystemResourceAccessor with the given directories/files as the roots.
     */
    public FileSystemResourceAccessor(File... baseDirsAndFiles) {
        for (File base : CollectionUtil.createIfNull(baseDirsAndFiles)) {
            if (!base.exists()) {
                Scope.getCurrentScope().getLog(getClass()).warning("Non-existent path: " + base.getAbsolutePath());
            } else if (base.isDirectory()) {
                addRootPath(base.toPath());
            } else if (base.getName().endsWith(".jar") || base.getName().toLowerCase().endsWith("zip")) {
                addRootPath(base.toPath());
            } else {
                throw new IllegalArgumentException(base.getAbsolutePath() + " must be a directory, jar or zip");
            }
        }
    }

    protected void addRootPath(Path path) {
        Scope.getCurrentScope().getLog(getClass()).fine("Adding path "+path+" to resourceAccessor "+getClass().getName());
        rootPaths.add(path);
    }

    protected LinkedHashSet<Path> getRootPaths() {
        return rootPaths;
    }

    protected File toFile(String path) {
        for (Path root : getRootPaths()) {
            File file = root.resolve(path).toFile();
            if (file.exists()) {
                return file;
            }
        }

        return new File(path);
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        streamPath = streamPath.replace("\\", "/");
        streamPath = streamPath.replaceFirst("^[\\\\/]([a-zA-Z]:)", "$1");
        final InputStreamList streams = new InputStreamList();

        streamPath = streamPath.replaceFirst("^/", ""); //Path is always relative to the file roots

        if (relativeTo != null) {
            relativeTo = relativeTo.replace("\\", "/");
            relativeTo = relativeTo.replaceFirst("^[\\\\/]([a-zA-Z]:)", "$1");
            relativeTo = relativeTo.replaceFirst("^/", ""); //Path is always relative to the file roots
        }

        for (Path rootPath : rootPaths) {
            URI streamURI = null;
            if (rootPath == null) {
                continue;
            }
            InputStream stream = null;
            if (isCompressedFile(rootPath)) {
                String finalPath = streamPath;

                // Can't close zipFile here, as we are (possibly) returning its child stream
                ZipFile zipFile = new ZipFile(rootPath.toFile());
                if (relativeTo != null) {
                    ZipEntry relativeEntry = zipFile.getEntry(relativeTo);
                    if (relativeEntry == null || relativeEntry.isDirectory()) {
                        //not a file, maybe a directory
                        finalPath = relativeTo + "/" + streamPath;
                    } else {
                        //is a file, find path relative to parent
                        String actualRelativeTo = relativeTo;
                        if (actualRelativeTo.contains("/")) {
                            actualRelativeTo = relativeTo.replaceFirst("/[^/]+?$", "");
                        } else {
                            actualRelativeTo = "";
                        }
                        finalPath = actualRelativeTo + "/" + streamPath;
                    }

                }

                //resolve any ..'s and duplicated /'s and convert back to standard '/' separator format
                finalPath = Paths.get(finalPath.replaceFirst("^/", "")).normalize().toString().replace("\\", "/");

                ZipEntry entry = zipFile.getEntry(finalPath);
                if (entry != null) {
                    // closing this stream will close zipFile
                    stream = new CloseChildWillCloseParentStream(zipFile.getInputStream(entry), zipFile);
                    streamURI = URI.create(rootPath.normalize().toUri() + "!" + entry.toString());
                } else {
                    zipFile.close();
                }
            } else {
                Path finalRootPath = rootPath;
                if (relativeTo != null) {
                    finalRootPath = finalRootPath.resolve(relativeTo);
                    File rootPathFile = finalRootPath.toFile();
                    if (rootPathFile.exists()) {
                        if (rootPathFile.isFile()) {
                            //relative to directory
                            finalRootPath = rootPathFile.getParentFile().toPath();
                        }
                    } else {
                        Scope.getCurrentScope().getLog(getClass()).fine("No relative path " + relativeTo + " in " + rootPath);
                        continue;
                    }
                }
                try {
                    if (Paths.get(streamPath).startsWith(finalRootPath) || Paths.get(streamPath).startsWith("/" + finalRootPath)) {
                        streamPath = finalRootPath.relativize(Paths.get(streamPath)).toString();
                    }
                } catch (InvalidPathException ignored) {
                    //that is ok
                }

                if (Paths.get(streamPath).isAbsolute()) {
                    continue; //on a windows system with an absolute path that doesn't start with rootPath
                }

                File resolvedFile = finalRootPath.resolve(streamPath).toFile();
                if (resolvedFile.exists()) {
                    streamURI = resolvedFile.getCanonicalFile().toURI();
                    stream = new BufferedInputStream(new FileInputStream(resolvedFile));
                }

            }

            if (stream != null) {
                if (streamPath.toLowerCase().endsWith(".gz")) {
                    stream = new GZIPInputStream(stream);
                }

                streams.add(streamURI, stream);
            }


        }

        return streams;
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        final SortedSet<String> returnList = new TreeSet<>();

        int maxDepth = recursive ? Integer.MAX_VALUE : 1;

        for (final Path rootPath : getRootPaths()) {
            SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (includeFiles && attrs.isRegularFile()) {
                        addToReturnList(file);
                    }
                    if (includeDirectories && attrs.isDirectory()) {
                        addToReturnList(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (includeDirectories) {
                        addToReturnList(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }

                protected void addToReturnList(Path file) {
                    String pathToAdd;
                    if (isCompressedFile(rootPath)) {
                        pathToAdd = file.normalize().toString().substring(1);  //pull off leading /
                    } else {
                        pathToAdd = rootPath.relativize(file).normalize().toString().replace("\\", "/");
                    }

                    pathToAdd = pathToAdd.replaceFirst("/$", "");
                    returnList.add(pathToAdd);
                }

            };


            if (isCompressedFile(rootPath)) {
                try (FileSystem fs = FileSystems.newFileSystem(rootPath, null)) {
                    Path basePath = fs.getRootDirectories().iterator().next();

                    if (relativeTo != null) {
                        basePath = basePath.resolve(relativeTo);
                        if (!Files.exists(basePath)) {
                            Scope.getCurrentScope().getLog(getClass()).info("Relative path "+relativeTo+" in "+rootPath+" does not exist");
                            continue;
                        } else if (Files.isRegularFile(basePath)) {
                            basePath = basePath.getParent();
                        }
                    }

                    if (path != null) {
                        basePath = basePath.resolve(path);
                    }

                    Files.walkFileTree(basePath, Collections.singleton(FileVisitOption.FOLLOW_LINKS), maxDepth, fileVisitor);
                } catch (NoSuchFileException e) {
                    //nothing to do, return null
                }
            } else {
                Path basePath = rootPath;

                if (relativeTo != null) {
                    basePath = basePath.resolve(relativeTo);
                    if (!Files.exists(basePath)) {
                        Scope.getCurrentScope().getLog(getClass()).info("Relative path "+relativeTo+" in "+rootPath+" does not exist");
                        continue;
                    } else if (Files.isRegularFile(basePath)) {
                        basePath = basePath.getParent();
                    }
                }


                if (path != null) {
                    if (path.startsWith("/") || path.startsWith("\\")) {
                        path = path.substring(1);
                    }

                    basePath = basePath.resolve(path);
                }

                if (!Files.exists(basePath)) {
                    continue;
                }
                Files.walkFileTree(basePath, Collections.singleton(FileVisitOption.FOLLOW_LINKS), maxDepth, fileVisitor);
            }
        }

        returnList.remove(path);
        return returnList;
    }

    /**
     * Returns true if the given path is a compressed file.
     */
    protected boolean isCompressedFile(Path path) {
        return path != null && (path.toString().startsWith("jar:") || path.toString().toLowerCase().endsWith(".jar") || path.toString().toLowerCase().endsWith(".zip"));
    }

    @Override
    public String toString() {
        return getClass().getName() + " (" + StringUtil.join(getRootPaths(), ", ", new StringUtil.ToStringFormatter()) + ")";
    }

    private static class CloseChildWillCloseParentStream extends FilterInputStream {

        private final Closeable parent;

        protected CloseChildWillCloseParentStream(InputStream in, Closeable parent) {
            super(in);
            this.parent = parent;
        }

        @Override
        public void close() throws IOException {
            super.close();
            parent.close();
        }
    }
}
