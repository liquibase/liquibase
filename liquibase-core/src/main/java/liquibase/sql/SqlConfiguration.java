package liquibase.sql;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.ConfigurationValueConverter;

import java.util.logging.Level;

/**
 * Configuration container for global properties.
 */
public class SqlConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<Level> SHOW_AT_LOG_LEVEL;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.sql");

        SHOW_AT_LOG_LEVEL = builder.define("logLevel", Level.class)
                .setDescription("Level to log SQL statements to")
                .setValueHandler(ConfigurationValueConverter.LOG_LEVEL)
                .setDefaultValue(Level.FINE)
                .build();
    }
}
