package liquibase.analytics.configuration;

import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalyticsConfigurationFactory extends AbstractPluginFactory<AnalyticsConfiguration> {
    @Override
    protected Class<AnalyticsConfiguration> getPluginClass() {
        return AnalyticsConfiguration.class;
    }

    @Override
    protected int getPriority(AnalyticsConfiguration obj, Object... args) {
        return obj.getPriority();
    }

    public AnalyticsConfiguration getPlugin() {
        return super.getPlugin();
    }
}
