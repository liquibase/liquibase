package liquibase.resource;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;

import java.util.ArrayList;
import java.util.List;

public class ResourceAccessorServiceFactory extends AbstractPluginFactory<ResourceAccessorService> {

    private List<ResourceAccessorService> registry = new ArrayList<>();

    private static ResourceAccessorServiceFactory instance;

    public static synchronized ResourceAccessorServiceFactory getInstance() {
        if (instance == null) {
            instance = new ResourceAccessorServiceFactory();
        }
        return instance;
    }

    private ResourceAccessorServiceFactory() {}

    @Override
    protected Class<ResourceAccessorService> getPluginClass() {
        return ResourceAccessorService.class;
    }

    @Override
    protected int getPriority(ResourceAccessorService obj, Object... args) {
        return obj.getPriority();
    }

    public ResourceAccessorService getResourceAccessorService() {
        return getPlugin();
    }
}
