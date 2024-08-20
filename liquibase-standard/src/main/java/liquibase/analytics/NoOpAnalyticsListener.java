package liquibase.analytics;

import liquibase.Scope;

public class NoOpAnalyticsListener implements UsageAnalyticsListener {
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public void handleEvent(Event event) {
        Scope.getCurrentScope().getLog(getClass()).fine("An analytics event has been received, but is being discarded because analytics is disabled.");
    }
}