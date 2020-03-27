package liquibase.resource;

import liquibase.Scope;
import liquibase.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * An implementation of {@link FileSystemResourceAccessor} that builds up the file roots based on the passed {@link ClassLoader}.
 * If you are using a ClassLoader that isn't based on local files, you will need to use a different {@link ResourceAccessor} implementation.
 *
 * @see OSGiResourceAccessor for OSGi-based classloaders
 */
public class ClassLoaderResourceAccessor extends AbstractResourceAccessor {

    private ClassLoader classLoader;
    protected List<FileSystem> rootPaths;

    public ClassLoaderResourceAccessor() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClassLoaderResourceAccessor(ClassLoader classLoader) {
        this.classLoader = classLoader;

    }

    /**
     * The classloader search logic in {@link #list(String, String, boolean, boolean, boolean)} does not handle jar files well.
     * This method is called by that method to populate {@link #rootPaths} with additional paths to search.
     */
    protected void loadRootPaths(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            final URL[] urls = ((URLClassLoader) classLoader).getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    try {
                        this.rootPaths.add(FileSystems.newFileSystem(Paths.get(url.toURI()), this.getClass().getClassLoader()));
                    } catch (ProviderNotFoundException e) {
                        if (url.toExternalForm().startsWith("file:/")) {
                            //that is expected, the classloader itself will handle it
                        } else {
                            Scope.getCurrentScope().getLog(getClass()).info("No filesystem provider for URL " + url.toExternalForm() + ". Will rely on classloader logic for listing files.");
                        }
                    } catch (Throwable e) {
                        Scope.getCurrentScope().getLog(getClass()).warning("Cannot create filesystem for url " + url.toExternalForm() + ": " + e.getMessage(), e);
                    }
                }
            }
        }

        final ClassLoader parent = classLoader.getParent();
        if (parent != null) {
            loadRootPaths(parent);
        }

    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        InputStreamList returnList = new InputStreamList();

        streamPath = getFinalPath(relativeTo, streamPath);

        //sometimes the classloader returns duplicate copies of the same url
        Set<String> seenUrls = new HashSet<>();

        Enumeration<URL> resources = classLoader.getResources(streamPath);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            if (seenUrls.add(url.toExternalForm())) {
                try {
                    returnList.add(url.toURI(), url.openStream());
                } catch (URISyntaxException e) {
                    Scope.getCurrentScope().getLog(getClass()).severe(e.getMessage(), e);
                }
            }
        }

        return returnList;
    }

    protected String getFinalPath(String relativeTo, String streamPath) {
        streamPath = streamPath.replace("\\", "/");
        streamPath = streamPath.replaceFirst("^classpath:", "");

        if (relativeTo != null) {
            relativeTo = relativeTo.replace("\\", "/");
            relativeTo = relativeTo.replaceFirst("^classpath:", "");
            relativeTo = relativeTo.replaceAll("//+", "/");

            if (!relativeTo.endsWith("/")) {
                String lastPortion = relativeTo.replaceFirst(".+/", "");
                if (lastPortion.contains(".")) {
                    relativeTo = relativeTo.replaceFirst("/[^/]+?$", "");
                }
            }

            //
            // If this is a simple file name then set the
            // relativeTo value as if it is a root path
            //
            if (! relativeTo.contains("/") && relativeTo.contains(".")) {
                relativeTo = "/";
            }
            streamPath = relativeTo + "/" + streamPath;
        }

        streamPath = streamPath.replaceAll("//+", "/");
        streamPath = streamPath.replaceFirst("^/", "");

        return streamPath;
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {

        String finalPath = getFinalPath(relativeTo, path);

        final SortedSet<String> returnList = listFromClassLoader(finalPath, recursive, includeFiles, includeDirectories);
        returnList.addAll(listFromRootPaths(finalPath, recursive, includeFiles, includeDirectories));

        return returnList;
    }

    /**
     * Called by {@link #list(String, String, boolean, boolean, boolean)} to find files in {@link #rootPaths}.
     */
    protected SortedSet<String> listFromRootPaths(String path, boolean recursive, boolean includeFiles, boolean includeDirectories) {
        if (rootPaths == null) {
            this.rootPaths = new ArrayList<>();

            loadRootPaths(classLoader);
        }
        SortedSet<String> returnSet = new TreeSet<>();

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
                if (!file.toString().equals(path)) {
                    returnSet.add(file.toString()
                            .replaceFirst("^/", "")
                            .replaceFirst("/$", "")
                            .replaceAll("//+", "/")
                    );
                }
            }
        };

        for (FileSystem fileSystem : rootPaths) {
            int maxDepth = recursive ? Integer.MAX_VALUE : 1;
            try {
                Files.walkFileTree(fileSystem.getPath(path), Collections.singleton(FileVisitOption.FOLLOW_LINKS), maxDepth, fileVisitor);
            } catch (NoSuchFileException e) {
                //that is OK
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Cannot walk filesystem: " + e.getMessage(), e);
            }
        }

        return returnSet;
    }

    /**
     * Called by {@link #list(String, String, boolean, boolean, boolean)} to find files in {@link #classLoader}.
     */
    protected SortedSet<String> listFromClassLoader(String path, boolean recursive, boolean includeFiles, boolean includeDirectories) {
        final SortedSet<String> returnSet = new TreeSet<>();

        final Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(path);
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).severe("Cannot list resources in path " + path + ": " + e.getMessage(), e);
            return returnSet;
        }

        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();

            try {
                final InputStream inputStream = url.openStream();

                final String fileList = StreamUtil.readStreamAsString(inputStream);
                if (!fileList.isEmpty()) {
                    for (String childName : fileList.split("\n")) {
                        String childPath = (path + "/" + childName).replaceAll("//+", "/");

                        if (isDirectory(childPath)) {
                            if (includeDirectories) {
                                returnSet.add(childPath);
                            }
                            if (recursive) {
                                returnSet.addAll(listFromClassLoader(childPath, recursive, includeFiles, includeDirectories));
                            }
                        } else {
                            if (includeFiles) {
                                returnSet.add(childPath);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(getClass()).severe("Cannot list resources in " + url.toExternalForm() + ": " + e.getMessage(), e);
            }
        }
        return returnSet;
    }

    /**
     * Used by {@link #listFromClassLoader(String, boolean, boolean, boolean)} to determine if a path is a directory or not.
     */
    protected boolean isDirectory(String path) {
        try {
            final Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();

                final File file = new File(url.toURI());
                if (file.exists() && file.isDirectory()) {
                    return true;
                }
            }
        } catch (Exception e) {
            //not a url we can handle
        }

        //fallback logic depends on files having an extension and directories not
        String lastPortion = path.replaceFirst(".*/", "");
        return !lastPortion.contains(".");
    }
}
