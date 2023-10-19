package liquibase.changeset;

import liquibase.Scope;
import liquibase.exception.LiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

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

    public ChangeSetService createChangeSetService() throws LiquibaseException {
        return getPlugin();
    }
}
