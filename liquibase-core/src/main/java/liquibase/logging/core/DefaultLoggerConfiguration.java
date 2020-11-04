package liquibase.logging.core;

import liquibase.configuration.AbstractConfigurationContainer;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.logging.Level;

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
    public Level getLogLevel() {
        String logLevel = getLogLevelName();

        if ("fine".equalsIgnoreCase(logLevel) || "debug".equalsIgnoreCase(logLevel)) {
            return Level.FINE;
        } else if ("info".equalsIgnoreCase(logLevel)) {
            return Level.INFO;
        } else if ("warning".equalsIgnoreCase(logLevel)) {
            return Level.WARNING;
        } else if ("error".equalsIgnoreCase(logLevel) || "severe".equalsIgnoreCase(logLevel)) {
            return Level.SEVERE;
        } else if ("off".equalsIgnoreCase(logLevel)) {
            return Level.OFF;
        } else {
            throw new UnexpectedLiquibaseException("Unknown log level: " + logLevel+".  Valid levels are: debug, info, warning, error, off");
        }
    }
    public DefaultLoggerConfiguration setLogLevel(String name) {
        getContainer().setValue(LOG_LEVEL, name);
        return this;
    }
}
