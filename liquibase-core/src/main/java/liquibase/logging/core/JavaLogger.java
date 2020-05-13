package liquibase.logging.core;

import java.util.logging.Level;

/**
 * The default logger for Liquibase. Routes messages through {@link java.util.logging.Logger}.
 */
public class JavaLogger extends AbstractLogger {

    private final String className;
    private java.util.logging.Logger logger;

    public JavaLogger(java.util.logging.Logger logger) {
        this.logger = logger;
        this.className = logger.getName();
    }

    @Override
    public void log(Level level, String message, Throwable e) {
        logger.logp(level, className, null, message, e);
    }
}
