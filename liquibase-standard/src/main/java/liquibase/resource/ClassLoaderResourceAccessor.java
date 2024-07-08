package liquibase.resource;

import liquibase.Scope;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * An implementation of {@link DirectoryResourceAccessor} that builds up the file roots based on the passed {@link ClassLoader}.
 * If you are using a ClassLoader that isn't based on local files, you will need to use a different {@link ResourceAccessor} implementation.
 *
 * @see OSGiResourceAccessor for OSGi-based classloaders
 */
public class ClassLoaderResourceAccessor extends AbstractResourceAccessor {

    private final ClassLoader classLoader;
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
        init();

        return additionalResourceAccessors.describeLocations();
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
                PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);

                for (URL url : urls) {
                    try {
                        if (url.getProtocol().equals("file")) {
                            additionalResourceAccessors.addResourceAccessor(pathHandlerFactory.getResourceAccessor(url.toExternalForm()));
                        }
                    } catch (FileNotFoundException e) {
                        //classloaders often have invalid paths specified on purpose. Just log them as fine level.
                        Scope.getCurrentScope().getLog(getClass()).fine("Classloader URL " + url.toExternalForm() + " does not exist", e);
                    } catch (Throwable e) {
                        Scope.getCurrentScope().getLog(getClass()).warning("Cannot handle classloader url " + url.toExternalForm() + ": " + e.getMessage()+". Operations that need to list files from this location may not work as expected", e);
                    }
                }
            }
        }

        final ClassLoader parent = classLoader.getParent();
        if (parent != null) {
            configureAdditionalResourceAccessors(parent);
        }
    }

    @Override
    public List<Resource> search(String path, SearchOptions searchOptions) throws IOException {
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
                returnList.addAll(resourceAccessor.search(path, searchOptions));
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        returnList.addAll(additionalResourceAccessors.search(path, searchOptions));


        return new ArrayList<>(returnList);
    }

    @Override
    public List<Resource> search(String path, boolean recursive) throws IOException {
        SearchOptions searchOptions = new SearchOptions();

        searchOptions.setRecursive(recursive);

        return search(path, searchOptions);
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
