package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsArgs;

public class NoOpAnalyticsListener implements AnalyticsListener {
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public void handleEvent(Event event) {
        Scope.getCurrentScope().getLog(getClass()).log(AnalyticsArgs.LOG_LEVEL.getCurrentValue(), "An analytics event has been received, but is being discarded because analytics is disabled.", null);
    }
}