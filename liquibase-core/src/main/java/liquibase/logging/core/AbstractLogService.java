package liquibase.logging.core;

import liquibase.logging.LogService;
import liquibase.logging.Logger;

import java.util.logging.Level;

/**
 * Convenience base implementation of a LoggerFactory.
 */
public abstract class AbstractLogService implements LogService {

    private Level logLevel;

    public AbstractLogService() {
        String defaultLoggerLevel = System.getProperty("liquibase.log.level");
        if (defaultLoggerLevel == null) {
            setLogLevel(Level.SEVERE);
        } else {
            setLogLevel(Level.parse(defaultLoggerLevel));
        }
    }

    @Override
    public Level getLogLevel() {
        return this.logLevel;
    }

    @Override
    public void setLogLevel(Level level) {
        this.logLevel = level;
    }

    @Override
    public void close() {

    }

}
