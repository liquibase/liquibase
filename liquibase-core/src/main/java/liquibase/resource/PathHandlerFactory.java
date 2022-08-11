package liquibase.resource;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Singleton for working with {@link PathHandler}s.
 */
public class PathHandlerFactory extends AbstractPluginFactory<PathHandler> {

    private PathHandlerFactory() {
    }

    @Override
    protected Class<PathHandler> getPluginClass() {
        return PathHandler.class;
    }

    @Override
    protected int getPriority(PathHandler obj, Object... args) {
        return obj.getPriority((String) args[0]);
    }

    /**
     * Creates a {@link ResourceAccessor} for the given path.
     */
    public ResourceAccessor getResourceAccessor(String root) throws IOException {
        final PathHandler plugin = getPlugin(root);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + root + "'");
        }
        return plugin.getResourceAccessor(root);
    }

    /**
     * Convenience method for {@link #getResource(String, boolean)} with false for fallbackToResourceAccessor
     */
    public Resource getResource(String resourcePath) throws IOException {
       return getResource(resourcePath, false);
    }

    /**
     * Creates a new resource at the specified path and returns an OutputStream for writing to it.
     *
     * @throws java.nio.file.FileAlreadyExistsException if the file already exists
     * @throws IOException if the path cannot be written to
     */
    public OutputStream createResource(String resourcePath) throws IOException {
        final PathHandler plugin = getPlugin(resourcePath);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + resourcePath + "'");
        }

        return plugin.createResource(resourcePath);
    }

    /**
     * Return the resource for the given path.
     * @param fallbackToResourceAccessor if true, check {@link Scope#getResourceAccessor()} before returning null
     * @return null if the resource does not exist
     *
     * @throws IOException if the path cannot be understood or if there is a problem parsing the path
     */
    public Resource getResource(String resourcePath, boolean fallbackToResourceAccessor) throws IOException {
        String searchPath = GlobalConfiguration.SEARCH_PATH.getCurrentValue();
        PathHandler plugin = determinePlugin(searchPath, resourcePath);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + resourcePath + "'");
        }
        Resource foundResource = plugin.getResource(resourcePath);
        if (foundResource == null && fallbackToResourceAccessor) {
            Scope.getCurrentScope().getLog(getClass()).fine("Did not find "+resourcePath+" directly. Checking search path");
            foundResource = Scope.getCurrentScope().getResourceAccessor().get(resourcePath);
        }

        return foundResource;
    }

    /**
     * Determine which PathHandler to use. If a search path is provided, the path handler associated with that
     * search path is always used. If no search path is provided, look for a path handler that is compatible
     * with the path provided.
     */
    private PathHandler determinePlugin(String searchPath, String resourcePath) {
        PathHandler plugin;
        if (StringUtil.isEmpty(searchPath)) {
            plugin = getPlugin(resourcePath);
        } else {
            plugin = getPlugin(GlobalConfiguration.SEARCH_PATH.getCurrentValue());
        }
        return plugin;
    }

    /**
     * Returns the outputStream from {@link #getResource(String, boolean)} if it exists, and the outputStream from {@link #createResource(String)} if it does not.
     * @return null if resoucePath does not exist and createIfNotExists is false
     * @throws IOException if there is an error opening the stream
     */
    public OutputStream openResourceOutputStream(String resourcePath, boolean fallbackToResourceAccessor, boolean createIfNotExists) throws IOException {
        Resource resource = getResource(resourcePath, fallbackToResourceAccessor);
        if (resource == null) {
            if (createIfNotExists) {
                return createResource(resourcePath);
            } else {
                return null;
            }
        }
        return resource.openOutputStream();
    }

    public boolean exists(String resourcePath) {
        final PathHandler plugin = getPlugin(resourcePath);
        return plugin.exists(resourcePath);
    }
}
