package liquibase.logging.core;

import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.AutoloadedConfigurations;

/**
 * Configuration container for {@link liquibase.logging.LogService} properties
 */
public class DefaultLoggerConfiguration implements AutoloadedConfigurations {

    public static ConfigurationDefinition<String> LOG_LEVEL;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.defaultlogger");
        LOG_LEVEL = builder.define("level", String.class)
                .setDescription("Logging level")
                .setDefaultValue("INFO")
                .build();
    }
}
