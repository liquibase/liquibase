package liquibase.analytics;

import liquibase.plugin.Plugin;

public interface UsageAnalyticsListener extends Plugin {

    int getPriority();

    void handleEvent(Event event);

}
