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

    private ResourceAccessorServiceFactory() {
        /*
        try {
            for (ResourceAccessorService service : Scope.getCurrentScope().getServiceLocator().findInstances(ResourceAccessorService.class)) {
                register(service);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
         */
    }

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

    public ResourceAccessor getPluginResourceAccessor() {
        ResourceAccessorService resourceAccessorService = getPlugin(); //ResourceAccessorServiceFactory.getInstance().getResourceAccessorService();
        return resourceAccessorService != null ? resourceAccessorService.getResourceAccessor() : null;
    }
}
