package liquibase.hub;

import liquibase.plugin.AbstractPluginFactory;

public class HubServiceFactory extends AbstractPluginFactory<HubService> {

    @Override
    protected Class<HubService> getPluginClass() {
        return HubService.class;
    }

    @Override
    protected int getPriority(HubService obj, Object... args) {
        return obj.getPriority();
    }

    public HubService getService() {
        return getPlugin();
    }
}
