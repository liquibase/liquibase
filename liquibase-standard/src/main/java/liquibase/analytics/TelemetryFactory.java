package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.TelemetryArgs;
import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TelemetryFactory extends AbstractPluginFactory<TelemetryListener> {

    @Override
    protected Class<TelemetryListener> getPluginClass() {
        return TelemetryListener.class;
    }

    @Override
    protected int getPriority(TelemetryListener obj, Object... args) {
        return obj.getPriority();
    }

    public void handleEvent(Event event) {
        try {
            if (TelemetryArgs.isTelemetryEnabled()) {
                getPlugin().handleEvent(event);
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to handle analytics event", e);
        }
    }
}