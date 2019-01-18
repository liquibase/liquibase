package liquibase.resource;

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
                log.warning("Non-existent path: " + base.getAbsolutePath());
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

        URI streamURI = null;
        for (Path rootPath : rootPaths) {
            InputStream stream = null;
            if (isCompressedFile(rootPath)) {
                try (ZipFile zipFile = new ZipFile(rootPath.toFile())) {
                    ZipEntry entry = zipFile.getEntry(streamPath);
                    if (entry != null) {
                        stream = zipFile.getInputStream(entry);
                        streamURI = URI.create(rootPath.normalize().toUri()+"!"+entry.toString());
                    }
                }
            } else {
                try {
                    if (Paths.get(streamPath).startsWith(rootPath)) {
                        streamPath = rootPath.relativize(Paths.get(streamPath)).toString();
                    }
                } catch (InvalidPathException ignored) {
                    //that is ok
                }

                if (streamPath.startsWith("/") || streamPath.startsWith("\\")) {
                    streamPath = streamPath.substring(1); //always relative to rootPath
                }

                if (Paths.get(streamPath).isAbsolute()) {
                    continue; //on a windows system with an absolute path that doesn't start with rootPath
                }

                File resolvedFile = rootPath.resolve(streamPath).toFile();
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

                    if (path != null) {
                        basePath = basePath.resolve(path);
                    }

                    Files.walkFileTree(basePath, Collections.singleton(FileVisitOption.FOLLOW_LINKS), maxDepth, fileVisitor);
                } catch (NoSuchFileException e) {
                    //nothing to do, return null
                }
            } else {
                Path basePath = rootPath;

                if (path != null) {
                    if (path.startsWith("/") || path.startsWith("\\")) {
                        path = path.substring(1);
                    }

                    basePath = basePath.resolve(path);
                }

                if (!basePath.toFile().exists()) {
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
        return path != null && (path.toString().toLowerCase().endsWith(".jar") || path.toString().toLowerCase().endsWith(".zip"));
    }

    @Override
    public String toString() {
        return getClass().getName() + " (" + StringUtil.join(getRootPaths(), ", ", new StringUtil.ToStringFormatter()) + ")";
    }


}
