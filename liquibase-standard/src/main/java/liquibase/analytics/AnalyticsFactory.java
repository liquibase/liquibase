package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsArgs;
import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalyticsFactory extends AbstractPluginFactory<AnalyticsListener> {

    @Override
    protected Class<AnalyticsListener> getPluginClass() {
        return AnalyticsListener.class;
    }

    @Override
    protected int getPriority(AnalyticsListener obj, Object... args) {
        return obj.getPriority();
    }

    public void handleEvent(Event event) {
        try {
            if (AnalyticsArgs.isTelemetryEnabled()) {
                getPlugin().handleEvent(event);
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to handle analytics event", e);
        }
    }
}