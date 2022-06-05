package liquibase.resource;

import liquibase.plugin.AbstractPluginFactory;

import java.io.IOException;

/**
 * Singleton for working with {@link ResourceRootParser}s.
 */
public class ResourceRootParserFactory extends AbstractPluginFactory<ResourceRootParser> {

    private ResourceRootParserFactory() {
    }

    @Override
    protected Class<ResourceRootParser> getPluginClass() {
        return ResourceRootParser.class;
    }

    @Override
    protected int getPriority(ResourceRootParser obj, Object... args) {
        return obj.getPriority((String) args[0]);
    }

    /**
     * Creates the {@link ResourceAccessor} for the given path.
     */
    public ResourceAccessor parse(String root) throws IOException {
        final ResourceRootParser plugin = getPlugin(root);
        if (plugin == null) {
            throw new IOException("Cannot parse resource location: '" + root + "'");
        }
        return plugin.parse(root);
    }
}
