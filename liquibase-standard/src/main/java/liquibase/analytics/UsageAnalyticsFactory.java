package liquibase.analytics;

import liquibase.Scope;
import liquibase.plugin.AbstractPluginFactory;

public class UsageAnalyticsFactory extends AbstractPluginFactory<UsageAnalyticsListener> {
    @Override
    protected Class<UsageAnalyticsListener> getPluginClass() {
        return UsageAnalyticsListener.class;
    }

    @Override
    protected int getPriority(UsageAnalyticsListener obj, Object... args) {
        return obj.getPriority();
    }

    public void handleEvent(Event event) {
        try {
            getPlugin().handleEvent(event);
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to handle analytics event", e);
        }
    }
}
