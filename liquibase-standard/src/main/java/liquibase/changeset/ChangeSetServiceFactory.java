package liquibase.changeset;

import liquibase.plugin.AbstractPluginFactory;

/**
 *
 * Create the appropriate ChangeSetService instance
 *
 */
public class ChangeSetServiceFactory extends AbstractPluginFactory<ChangeSetService> {

    private static ChangeSetServiceFactory factory = null;

    private ChangeSetServiceFactory() {
    }

    public static ChangeSetServiceFactory getInstance() {
        if (factory == null) {
            factory = new ChangeSetServiceFactory();
        }
        return factory;
    }

    @Override
    protected Class<ChangeSetService> getPluginClass() {
        return ChangeSetService.class;
    }

    @Override
    protected int getPriority(ChangeSetService obj, Object... args) {
        return obj.getPriority();
    }

    public ChangeSetService createChangeSetService() {
        return getPlugin();
    }
}