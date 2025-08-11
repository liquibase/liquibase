package liquibase.analytics.configuration;

import liquibase.analytics.AnalyticsListener;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AnalyticsArgs implements AutoloadedConfigurations {

    /**
     * Do not access this value directly to check whether analytics are enabled.
     * Instead, use the method {@link AnalyticsListener#isEnabled()}
     */
    public static final ConfigurationDefinition<Boolean> ENABLED;
    public static final ConfigurationDefinition<String> CONFIG_ENDPOINT_URL;
    public static final ConfigurationDefinition<Boolean> DEV_OVERRIDE;
    public static final ConfigurationDefinition<Integer> CONFIG_ENDPOINT_TIMEOUT_MILLIS;
    public static final ConfigurationDefinition<Level> LOG_LEVEL;
    public static final ConfigurationDefinition<Integer> LICENSE_KEY_CHARS;
    public static final ConfigurationDefinition<Integer> TIMEOUT_MILLIS;
    public static final ConfigurationDefinition<Long> CONFIG_CACHE_TIMEOUT_MILLIS;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.analytics");

        ENABLED = builder.define("enabled", Boolean.class)
                .setDescription("Enable or disable sending product usage data and analytics to Liquibase. Learn more at https://docs.liquibase.com/analytics.")
                .build();

        CONFIG_ENDPOINT_URL = builder.define("configEndpointUrl", String.class)
                .setDefaultValue("https://config.liquibase.com/analytics.yaml")
                .setHidden(true)
                .build();

        DEV_OVERRIDE = builder.define("devOverride", Boolean.class)
                .setDescription("By default, Liquibase will not send analytics in dev (non release) builds. To override this behavior, set this value to true and provide a value for " + CONFIG_ENDPOINT_URL.getKey())
                .setHidden(true)
                .setDefaultValue(false)
                .build();

        TIMEOUT_MILLIS = builder.define("timeoutMillis", Integer.class)
                .setHidden(true)
                .setDescription("By default, the timeout for sending data to the remote endpoint is configured in the config endpoint. Any value set here will override that value.")
                .build();

        CONFIG_ENDPOINT_TIMEOUT_MILLIS = builder.define("configEndpointTimeoutMillis", Integer.class)
                .setDefaultValue(1500)
                .setHidden(true)
                .build();

        LOG_LEVEL = builder.define("logLevel", Level.class)
                .setDefaultValue(Level.OFF)
                .setHidden(true)
                .build();

        LICENSE_KEY_CHARS = builder.define("licenseKeyChars", Integer.class)
                .setDefaultValue(12)
                .setHidden(true)
                .setDescription("Number of characters of the license key that should be appended to the userId. This is used in the event that the same customer has multiple license keys associated with them.")
                .setValueHandler((value -> {
                    int maxChars = 36;
                    try {
                        Integer chars = Integer.valueOf(String.valueOf(value));
                        return Math.min(chars, maxChars);
                    } catch (Exception e) {
                        return maxChars;
                    }
                }))
                .build();

        CONFIG_CACHE_TIMEOUT_MILLIS = builder.define("configCacheTimeoutMillis", Long.class)
                .setDefaultValue(TimeUnit.MINUTES.toMillis(60))
                .setDescription("Liquibase caches the results from the config endpoint, and this value determines how long that cache should live for before being refreshed.")
                .setHidden(true)
                .build();
    }
}
