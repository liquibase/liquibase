package liquibase.logging.core;

import liquibase.logging.LogLevel;
import liquibase.logging.LogTarget;
import org.slf4j.Logger;

/**
 * The default logger for this software. The general output format created by this logger is:
 * [log level] date/time: liquibase: DatabaseChangeLog name:ChangeSet name: message.
 * DEBUG/SQL/INFO message are printer to STDOUT and WARNING/ERROR messages are printed to STDERR.
 */
public class Slf4jLogger implements liquibase.logging.Logger {

    private org.slf4j.Logger logger;

    public Slf4jLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public LogLevel getLogLevel() {
        if (logger.isTraceEnabled()) {
            return LogLevel.TRACE;
        } else if (logger.isDebugEnabled()) {
            return LogLevel.DEBUG;
        } else if (logger.isInfoEnabled()) {
            return LogLevel.INFO;
        } else if (logger.isWarnEnabled()) {
            return LogLevel.WARNING;
        } else {
            return LogLevel.ERROR;
        }
    }

    @Override
    public void error(LogTarget target, String message)  {
        logger.error(message);
    }

    @Override
    public void error(LogTarget target, String message, Throwable e) {
        logger.error(message, e);
    }

    @Override
    public void warn(LogTarget target, String message) {
        logger.warn(message);
    }

    @Override
    public void warn(LogTarget target, String message, Throwable e) {
        logger.warn(message, e);
    }

    @Override
    public void info(LogTarget logTarget, String message) {
        logger.info(message);
    }

    @Override
    public void info(LogTarget target, String message, Throwable e) {
        logger.info(message, e);
    }

    @Override
    public void sql(LogTarget target, String sqlStatement) {
        logger.trace(sqlStatement);
    }

    @Override
    public void debug(LogTarget target, String message) {
        logger.debug(message);
    }

    @Override
    public void debug(LogTarget target, String message, Throwable e) {
        logger.debug(message, e);
    }
}
