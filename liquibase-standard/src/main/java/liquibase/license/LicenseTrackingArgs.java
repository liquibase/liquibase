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
    public static final ConfigurationDefinition<String> USER;

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

        USER = builder.define("user", String.class)
                .setDescription("Identify the group or system that is using Liquibase, defaults to hostname if not specified")
                .build();
    }
}
