package liquibase.resource;

import liquibase.plugin.AbstractPluginFactory;

import java.io.IOException;
import java.io.InputStream;

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
     * Creates the {@link ResourceAccessor} for the given path.
     */
    public ResourceAccessor getResourceAccessor(String root) throws IOException {
        final PathHandler plugin = getPlugin(root);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + root + "'");
        }
        return plugin.getResourceAccessor(root);
    }


    public Resource getResource(String resource) throws IOException {
        final PathHandler plugin = getPlugin(resource);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + resource + "'");
        }
        return plugin.getResource(resource);
    }
}
