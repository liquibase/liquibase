package liquibase.telemetry.configuration;

import liquibase.plugin.AbstractPluginFactory;

public class TelemetryConfigurationFactory extends AbstractPluginFactory<TelemetryConfiguration> {
    @Override
    protected Class<TelemetryConfiguration> getPluginClass() {
        return TelemetryConfiguration.class;
    }

    @Override
    protected int getPriority(TelemetryConfiguration obj, Object... args) {
        return obj.getPriority();
    }

    public TelemetryConfiguration getPlugin() {
        return super.getPlugin();
    }
}
