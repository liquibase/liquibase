package liquibase.resource;

import liquibase.Scope;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * An implementation of {@link DirectoryResourceAccessor} that builds up the file roots based on the passed {@link ClassLoader}.
 * If you are using a ClassLoader that isn't based on local files, you will need to use a different {@link ResourceAccessor} implementation.
 *
 * @see OSGiResourceAccessor for OSGi-based classloaders
 */
public class ClassLoaderResourceAccessor extends CompositeResourceAccessor {

    private ClassLoader classLoader;
    private boolean initialized = false;
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
        return super.describeLocations();
    }

    /**
     * Performs the configuration of this resourceAccessor.
     * Not done in the constructor for performance reasons, but can be called at the beginning of every public method.
     */
    protected void init() {
        if (!initialized) {
            this.description = new TreeSet<>();
            loadRootPaths(classLoader);
            initialized  = true;
        }
    }

    /**
     * The classloader search logic in {@link #search(String, boolean)} does not handle jar files well.
     * This method is called by that method to call {@link #addResourceAccessor(ResourceAccessor)} with paths to search.
     */
    protected void loadRootPaths(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            final URL[] urls = ((URLClassLoader) classLoader).getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    try {
                        addDescription(url);
                        Path path = Paths.get(url.toURI());
                        String lowerCaseName = path.getFileName().toString().toLowerCase();
                        if (lowerCaseName.endsWith(".jar") || lowerCaseName.endsWith("zip")) {
                            addResourceAccessor(new ZipResourceAccessor(path));
                        } else {
                            addResourceAccessor(new DirectoryResourceAccessor(path));
                        }
                    } catch (Throwable e) {
                        Scope.getCurrentScope().getLog(getClass()).warning("Cannot create resourceAccessor for url " + url.toExternalForm() + ": " + e.getMessage(), e);
                    }
                }
            }
        }

        final ClassLoader parent = classLoader.getParent();
        if (parent != null) {
            loadRootPaths(parent);
        }

    }

    private void addDescription(URL url) {
        try {
            this.description.add(Paths.get(url.toURI()).toString());
        } catch (Throwable e) {
            this.description.add(url.toExternalForm());
        }
    }

    @Override
    public List<Resource> search(String path, boolean recursive) throws IOException {
        init();
        return super.search(path, recursive);
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
