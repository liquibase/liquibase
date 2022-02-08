package liquibase.resource;

import liquibase.Scope;
import liquibase.plugin.AbstractPluginFactory;

public class ResourceAccessorServiceFactory extends AbstractPluginFactory<ResourceAccessorService> {

    protected ResourceAccessorServiceFactory() {}

    @Override
    protected Class<ResourceAccessorService> getPluginClass() {
        return ResourceAccessorService.class;
    }

    @Override
    protected int getPriority(ResourceAccessorService obj, Object... args) {
        return 0;
    }

    public ResourceAccessorService getResourceAccessorService() {
        return getPlugin();
    }

    public static ResourceAccessor getPluginResourceAccessor(String filePath) {
        ResourceAccessorService resourceAccessorService =
            Scope.getCurrentScope().getSingleton(ResourceAccessorServiceFactory.class).getResourceAccessorService();
        ResourceAccessor localResourceAccessor = null;
        if (resourceAccessorService != null) {
            localResourceAccessor = resourceAccessorService.getResourceAccessor(filePath);
        }
        return localResourceAccessor;
    }
}
