package liquibase.logging.core;

import liquibase.configuration.AbstractConfigurationContainer;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogLevel;

/**
 * Configuration container for {@link liquibase.logging.LogService} properties
 */
public class DefaultLoggerConfiguration extends AbstractConfigurationContainer {

    public static final String LOG_LEVEL = "level";

    public DefaultLoggerConfiguration() {
        super("liquibase.defaultlogger");

        getContainer().addProperty(LOG_LEVEL, String.class)
                .setDescription("Logging level")
                .setDefaultValue("INFO");
    }

    public String getLogLevelName() {
        return getContainer().getValue(LOG_LEVEL, String.class);
    }


    /**
     * Transforms the strings DEBUG, INFO, WARNING, ERROR and OFF (case-insensitive) into the appropriate LogLevel.
     * @return a value from the LogLevel enum
     */
    public LogLevel getLogLevel() {
        String logLevel = getLogLevelName();

        if ("debug".equalsIgnoreCase(logLevel)) {
            return LogLevel.DEBUG;
        } else if ("info".equalsIgnoreCase(logLevel)) {
            return LogLevel.INFO;
        } else if ("warning".equalsIgnoreCase(logLevel)) {
            return LogLevel.WARNING;
        } else if ("error".equalsIgnoreCase(logLevel) || "severe".equalsIgnoreCase(logLevel)) {
            return LogLevel.SEVERE;
        } else if ("off".equalsIgnoreCase(logLevel)) {
            return LogLevel.OFF;
        } else {
            throw new UnexpectedLiquibaseException("Unknown log level: " + logLevel+".  Valid levels are: debug, info, warning, error, off");
        }
    }
    public DefaultLoggerConfiguration setLogLevel(String name) {
        getContainer().setValue(LOG_LEVEL, name);
        return this;
    }
}
