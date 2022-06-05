package liquibase.resource;

import liquibase.plugin.AbstractPluginFactory;

import java.io.IOException;

/**
 * Singleton for working with {@link SearchPathParser}s.
 */
public class SearchPathParserFactory extends AbstractPluginFactory<SearchPathParser> {

    private SearchPathParserFactory() {
    }

    @Override
    protected Class<SearchPathParser> getPluginClass() {
        return SearchPathParser.class;
    }

    @Override
    protected int getPriority(SearchPathParser obj, Object... args) {
        return obj.getPriority((String) args[0]);
    }

    /**
     * Creates the {@link ResourceAccessor} for the given path.
     */
    public ResourceAccessor parse(String root) throws IOException {
        final SearchPathParser plugin = getPlugin(root);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + root + "'");
        }
        return plugin.parse(root);
    }
}
