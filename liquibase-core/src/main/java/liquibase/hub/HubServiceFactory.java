package liquibase.hub;

import liquibase.plugin.AbstractPluginFactory;

public class HubServiceFactory extends AbstractPluginFactory<HubService> {

    private String offlineReason;

    protected HubServiceFactory() {
    }

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

    public boolean isOnline() {
        return getService().isOnline();
    }

    public String getOfflineReason() {
        return offlineReason;
    }

    public void setOfflineReason(String offlineReason) {
        this.offlineReason = offlineReason;
    }
}
