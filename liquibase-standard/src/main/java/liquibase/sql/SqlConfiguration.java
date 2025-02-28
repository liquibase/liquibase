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

    public static final ConfigurationDefinition<Boolean> ALWAYS_SET_FETCH_SIZE;

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
        ALWAYS_SET_FETCH_SIZE = builder.define("alwaysSetFetchSize", Boolean.class)
                .setDescription("If true, all queries will have their fetch size set to the fetch size defined in their database implementation. This has the effect of informing the driver how many rows should be fetched when processing the result set. This is not guaranteed to be respected by the driver, but if respected, should improve query performance significantly.")
                .setDefaultValue(Boolean.TRUE)
                .setHidden(true)
                .build();
    }
}
