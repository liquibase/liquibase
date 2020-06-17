package liquibase.logging.core;

import liquibase.logging.LogLevel;
import liquibase.logging.LogType;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

/**
 * The default logger for Liquibase. Routes messages through SLF4j.
 * The command line app uses logback, but this logger can use any slf4j binding.
 */
public class Slf4jLogger extends AbstractLogger {

    private org.slf4j.Logger logger;

    public Slf4jLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void severe(LogType target, String message) {
        logger.error(MarkerFactory.getMarker(target.name()), message);
    }

    @Override
    public void severe(LogType target, String message, Throwable e) {
        logger.error(MarkerFactory.getMarker(target.name()), message, e);
    }

    @Override
    public void warning(LogType target, String message) {
        logger.warn(MarkerFactory.getMarker(target.name()), message);
    }

    @Override
    public void warning(LogType target, String message, Throwable e) {
        logger.warn(MarkerFactory.getMarker(target.name()), message, e);
    }

    @Override
    public void info(LogType target, String message) {
        logger.info(MarkerFactory.getMarker(target.name()), message);
    }

    @Override
    public void info(LogType target, String message, Throwable e) {
        logger.info(MarkerFactory.getMarker(target.name()), message, e);
    }

    @Override
    public void debug(LogType target, String message) {
        logger.debug(MarkerFactory.getMarker(target.name()), message);
    }

    @Override
    public void debug(LogType target, String message, Throwable e) {
        logger.debug(MarkerFactory.getMarker(target.name()), message, e);
    }
}
