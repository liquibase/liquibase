package liquibase.resource;

import liquibase.Scope;
import liquibase.plugin.AbstractPluginFactory;

import java.io.IOException;

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
     * Return the resource for the given path.
     * @param fallbackToResourceAccessor if true, check {@link Scope#getResourceAccessor()} before returning null
     * @return null if the resource does not exist
     *
     * @throws IOException if the path cannot be understood or if there is a problem parsing the path
     */
    public Resource getResource(String resourcePath, boolean fallbackToResourceAccessor) throws IOException {
        final PathHandler plugin = getPlugin(resourcePath);
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
}
