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

    @Override
    public boolean isEnabled() {
        // Safe to mark this as always enabled, as the default for this is to
        // not send any analytics and the priority of LiquibaseAnalyticsListener
        // will be marked higher than this class when {@link AnalyticsArgs#ENABLED}
        // is set to true
        return true;
    }
}