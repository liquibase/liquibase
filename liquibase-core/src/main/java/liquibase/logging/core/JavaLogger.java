package liquibase.logging.core;

import liquibase.logging.LogType;

import java.util.logging.Level;

/**
 * The default logger for Liquibase. Routes messages through {@link java.util.logging.Logger}.
 */
public class JavaLogger extends AbstractLogger {

    private java.util.logging.Logger logger;

    public JavaLogger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(Level level, LogType target, String message, Throwable e) {
        logger.log(level, message, e);
    }
}
