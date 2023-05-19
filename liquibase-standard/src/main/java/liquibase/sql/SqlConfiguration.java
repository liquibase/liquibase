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
    public static final ConfigurationDefinition<Boolean> SHOW_SQL_WARNING_MESSAGES;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.sql");

        SHOW_AT_LOG_LEVEL = builder.define("logLevel", Level.class)
                .setDescription("Level to log SQL statements to")
                .setValueHandler(ConfigurationValueConverter.LOG_LEVEL)
                .setDefaultValue(Level.FINE)
                .build();
        SHOW_SQL_WARNING_MESSAGES = builder.define("showSqlWarnings", Boolean.class)
                .setDescription("Show SQLWarning messages")
                .setDefaultValue(Boolean.TRUE)
                .build();
    }
}
