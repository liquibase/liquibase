package liquibase.resource;

import liquibase.plugin.AbstractPluginFactory;

import java.io.IOException;

/**
 * Singleton for working with {@link SearchPathHandler}s.
 */
public class SearchPathHandlerFactory extends AbstractPluginFactory<SearchPathHandler> {

    private SearchPathHandlerFactory() {
    }

    @Override
    protected Class<SearchPathHandler> getPluginClass() {
        return SearchPathHandler.class;
    }

    @Override
    protected int getPriority(SearchPathHandler obj, Object... args) {
        return obj.getPriority((String) args[0]);
    }

    /**
     * Creates the {@link ResourceAccessor} for the given path.
     */
    public ResourceAccessor getResourceAccessor(String root) throws IOException {
        final SearchPathHandler plugin = getPlugin(root);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + root + "'");
        }
        return plugin.getResourceAccessor(root);
    }
}
