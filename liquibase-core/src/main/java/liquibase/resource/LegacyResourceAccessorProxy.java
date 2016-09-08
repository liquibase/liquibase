package liquibase.resource;

import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.list.ListHandler;
import liquibase.util.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Has all the workarounds which where included in pre 3.6.x versions.
 */
public final class LegacyResourceAccessorProxy implements ResourceAccessor {

    private final ResourceAccessor proxy;
    private final Logger logger;
    private boolean enableFuzyRootSlash = false;
    private boolean enableCheckFileSystem = false;
    private ThreadContextClassLoader classLoader = null;
    private String pathCleanup = null;

    public LegacyResourceAccessorProxy(ResourceAccessor proxy) {
        this.proxy = Validate.notNullArgument(proxy, "Can't create legacy proxy with null ResourceAccessor");
        this.logger = LogFactory.getInstance().getLog();
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String pathRaw) throws IOException {
        String path = pathCleanup(pathRaw);
        Set<InputStream> returnSet = proxy.getResourcesAsStream(path);
        if (returnSet != null && returnSet.size() > 0) {
            return returnSet;
        }
        for (String altPath : getAlternatePaths(path)) {
            Set<InputStream> altReturnSet = proxy.getResourcesAsStream(altPath);
            if (altReturnSet != null && altReturnSet.size() > 0) {
                return returnSet;
            }
        }
        if (enableCheckFileSystem) {
            return fullSystemPathWorkaround(path);
        }
        return null;
    }

    @Override
    public Set<String> list(String relativeTo, String pathRaw, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        String path = pathCleanup(pathRaw);
        Set<String> returnSet = new HashSet<String>();
        Set<String> thisSet = proxy.list(relativeTo, path, includeFiles, includeDirectories, recursive);
        if (thisSet != null) {
            returnSet.addAll(thisSet);
        }
        for (String altPath : getAlternatePaths(path)) {
            returnSet.addAll(proxy.list(relativeTo, altPath, includeFiles, includeDirectories, recursive));
        }
        if (!returnSet.isEmpty()) {
            return returnSet;
        }
        return null;
    }

    @Override
    public ClassLoader toClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        return proxy.toClassLoader();
    }

    public LegacyResourceAccessorProxy enableThreadContextClassLoader() {
        logger.debug(getClass().getSimpleName()+" enableThreadContextClassLoader");
        this.classLoader = new ThreadContextClassLoader();
        return this;
    }

    /**
     * Extension of that adds extra fuzzy searching logic based on
     * what users may enter that is different than what is exactly correct.
     */
    public LegacyResourceAccessorProxy enableFuzyRootSlash() {
        logger.debug(getClass().getSimpleName()+" enableFuzyRootSlash");
        this.enableFuzyRootSlash = true;
        return this;
    }

    /**
     * Removes the matched regex from the path string.
     */
    public LegacyResourceAccessorProxy enablePathCleanup(String matchRegex) {
        logger.debug(getClass().getSimpleName()+" enablePathCleanup "+matchRegex);
        this.pathCleanup = matchRegex;
        return this;
    }

    /**
     * Cleans up the path if enabled.
     */
    private String pathCleanup(String path) {
        if (pathCleanup == null) {
            return path;
        }
        return path.replaceFirst(pathCleanup, "");
    }

    /**
     * Return alternate options for the given path that the user maybe meant. Return in order of likelihood.
     */
    private List<String> getAlternatePaths(String path) {
        if (enableFuzyRootSlash && path.startsWith(ListHandler.RESOURCE_PATH_SEPERATOR)) { //People are often confused about leading slashes in resource paths...
            return Collections.singletonList((path.substring(1)));
        }
        return Collections.emptyList();
    }

    /**
     * Extension of that adds an extra inputstream loading from full system path urls.
     */
    public LegacyResourceAccessorProxy enableCheckFileSystem() {
        logger.debug(getClass().getSimpleName()+" enableCheckFileSystem");
        this.enableCheckFileSystem = true;
        return this;
    }

    private Set<InputStream> fullSystemPathWorkaround(String path) {
        try {
            return Collections.singleton(InputStream.class.cast(new FileInputStream(new File(path))));
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    private class ThreadContextClassLoader extends ClassLoader {

        @Override
        public Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
            // Try First - the context class loader associated with the current thread. Often used in j2ee servers.
            // Note: The contextClassLoader cannot be added to the classLoaders list up front as the thread that constructs
            // liquibase is potentially different to thread that uses it.
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                try {
                    Class<?> classe=contextClassLoader.loadClass(name);
                    if(resolve) {
                        resolveClass(classe);
                    }
                    return classe;
                } catch (ClassNotFoundException notFound) {
                    // ok.. try proxy
                }
            }
            return proxy.toClassLoader().loadClass(name);
        }
    }
}
