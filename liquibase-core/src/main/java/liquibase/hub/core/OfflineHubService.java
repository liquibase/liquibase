package liquibase.hub.core;

import liquibase.hub.HubService;
import liquibase.plugin.Plugin;

public class OfflineHubService extends OnlineHubService implements HubService {
    @Override
    public int getPriority() {
        return Plugin.PRIORITY_DEFAULT;
    }

    @Override
    public boolean hasApiKey() {
        return false;
    }
}
