package liquibase.license;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

import java.util.logging.Level;

public class LicenseTrackingArgs implements AutoloadedConfigurations {

    /**
     * Do not access this value directly to check whether analytics are enabled.
     * Instead, use the method {@link AnalyticsArgs#isAnalyticsEnabled}
     */
    public static final ConfigurationDefinition<Boolean> ENABLED;
    public static final ConfigurationDefinition<String> URL;
    public static final ConfigurationDefinition<Level> LOG_LEVEL;
    public static final ConfigurationDefinition<String> TRACKING_ID;
    public static final ConfigurationDefinition<Integer> TIMEOUT;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.license.utility");

        ENABLED = builder.define("enabled", Boolean.class)
                .setDescription("Enable or disable sending license usage data.")
                .setDefaultValue(false)
                .build();

        URL = builder.define("url", String.class)
                .setDefaultValue("http://localhost:8080")
                .build();

        LOG_LEVEL = builder.define("logLevel", Level.class)
                .setDefaultValue(Level.INFO)
                .build();

        TRACKING_ID = builder.define("trackingId", String.class)
                .setDescription("Specifies an identifier (e.g., team name, pipeline ID, or environment) to track and analyze Liquibase license usage. If not provided, the hostname and user is used for identification.")
                .build();

        TIMEOUT = builder.define("timeout", Integer.class)
                .setDescription("Time, in milliseconds, to wait for HTTP request to complete")
                .setDefaultValue(1500)
                .setHidden(true)
                .build();
    }
}
