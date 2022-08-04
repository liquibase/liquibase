package liquibase.resource;

import liquibase.Scope;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * An implementation of {@link DirectoryResourceAccessor} that builds up the file roots based on the passed {@link ClassLoader}.
 * If you are using a ClassLoader that isn't based on local files, you will need to use a different {@link ResourceAccessor} implementation.
 *
 * @see OSGiResourceAccessor for OSGi-based classloaders
 */
public class ClassLoaderResourceAccessor extends AbstractResourceAccessor {

    private ClassLoader classLoader;
    private CompositeResourceAccessor additionalResourceAccessors;
    protected SortedSet<String> description;

    public ClassLoaderResourceAccessor() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClassLoaderResourceAccessor(ClassLoader classLoader) {
        this.classLoader = classLoader;

    }

    @Override
    public List<String> describeLocations() {
        return Collections.singletonList("Configured classpath");
    }

    @Override
    public void close() throws Exception {
        if (additionalResourceAccessors != null) {
            additionalResourceAccessors.close();
        }
    }

    /**
     * Performs the configuration of this resourceAccessor.
     * Not done in the constructor for performance reasons, but can be called at the beginning of every public method.
     */
    protected synchronized void init() {
        if (additionalResourceAccessors == null) {
            this.description = new TreeSet<>();
            this.additionalResourceAccessors = new CompositeResourceAccessor();

            configureAdditionalResourceAccessors(classLoader);
        }
    }

    /**
     * The classloader search logic in {@link #search(String, boolean)} does not handle jar files well.
     * This method is called by that method to configure an internal {@link ResourceAccessor} with paths to search.
     */
    protected void configureAdditionalResourceAccessors(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            final URL[] urls = ((URLClassLoader) classLoader).getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    try {
                        if (url.getProtocol().equals("file")) {
                            String filename = url.getFile().toLowerCase(Locale.ROOT);
                            if (filename.endsWith(".zip") || filename.endsWith(".jar")) {
                                Path path = Paths.get(url.toURI());
                                additionalResourceAccessors.addResourceAccessor(new ZipResourceAccessor(path));
                            }
                        }
                    } catch (Throwable e) {
                        Scope.getCurrentScope().getLog(getClass()).warning("Cannot create resourceAccessor for url " + url.toExternalForm() + ": " + e.getMessage(), e);
                    }
                }
            }
        }

        final ClassLoader parent = classLoader.getParent();
        if (parent != null) {
            configureAdditionalResourceAccessors(parent);
        }
    }
//
//    private void addDescription(URL url) {
//        try {
//            this.description.add(Paths.get(url.toURI()).toString());
//        } catch (Throwable e) {
//            this.description.add(url.toExternalForm());
//        }
//    }

    @Override
    public List<Resource> search(String path, boolean recursive) throws IOException {
        init();

        final LinkedHashSet<Resource> returnList = new LinkedHashSet<>();
        PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);

        final Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(path);
        } catch (IOException e) {
            throw new IOException("Cannot list resources in path " + path + ": " + e.getMessage(), e);
        }

        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();

            String urlExternalForm = url.toExternalForm();
            urlExternalForm = urlExternalForm.replaceFirst(Pattern.quote(path) + "/?$", "");

            try (ResourceAccessor resourceAccessor = pathHandlerFactory.getResourceAccessor(urlExternalForm)) {
                returnList.addAll(resourceAccessor.search(path, recursive));
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        returnList.addAll(additionalResourceAccessors.search(path, recursive));


        return new ArrayList<>(returnList);
    }

    @Override
    public List<Resource> getAll(String path) throws IOException {
        //using a hash because sometimes the same resource gets included multiple times.
        LinkedHashSet<Resource> returnList = new LinkedHashSet<>();

        path = path.replace("\\", "/").replaceFirst("^/", "");

        Enumeration<URL> all = classLoader.getResources(path);
        try {
            while (all.hasMoreElements()) {
                URI uri = all.nextElement().toURI();
                if (uri.getScheme().equals("file")) {
                    returnList.add(new PathResource(path, Paths.get(uri)));
                } else {
                    returnList.add(new URIResource(path, uri));
                }
            }
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage(), e);
        }

        if (returnList.size() == 0) {
            return null;
        }
        return new ArrayList<>(returnList);
    }
}
