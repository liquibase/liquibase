package liquibase.logging.core;

import liquibase.logging.LogMessageFilter;

import java.util.logging.Level;

/**
 * The default logger for Liquibase. Routes messages through {@link java.util.logging.Logger}.
 */
public class JavaLogger extends AbstractLogger {

    private final String className;
    private final java.util.logging.Logger logger;

    /**
     * @deprecated use {@link #JavaLogger(java.util.logging.Logger)}
     */
    @Deprecated
    public JavaLogger(java.util.logging.Logger logger, LogMessageFilter filter) {
       this(logger);
    }

    public JavaLogger(java.util.logging.Logger logger) {
        this.logger = logger;
        this.className = logger.getName();
    }

    @Override
    public void log(Level level, String message, Throwable e) {
        if (level.equals(Level.OFF)) {
            return;
        }

        if (!logger.isLoggable(level)) {
            return;
        }
        logger.logp(level, className, null, message, e);
    }
}
