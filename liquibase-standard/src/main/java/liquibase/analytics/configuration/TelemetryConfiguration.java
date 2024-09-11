package liquibase.analytics.configuration;

import liquibase.plugin.Plugin;

public interface TelemetryConfiguration extends Plugin {
    int getPriority();

    boolean isOssTelemetryEnabled() throws Exception;
    boolean isProTelemetryEnabled() throws Exception;
}
