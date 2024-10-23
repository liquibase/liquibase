package liquibase.analytics.configuration;

import liquibase.plugin.Plugin;

public interface AnalyticsConfiguration extends Plugin {
    int getPriority();

    boolean isOssAnalyticsEnabled() throws Exception;
    boolean isProAnalyticsEnabled() throws Exception;
}
