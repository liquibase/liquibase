package liquibase.logging.core;

import liquibase.logging.LogType;

import java.util.logging.Level;

/**
 * The default logger for Liquibase. Routes messages through SLF4j.
 * The command line app uses logback, but this logger can use any slf4j binding.
 */
public class JavaLogger extends AbstractLogger {

    private java.util.logging.Logger logger;

    public JavaLogger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setLogLevel(Level level) {
        this.logger.setLevel(level);
    }

    @Override
    public Level getLogLevel() {
        return this.logger.getLevel();
    }

    @Override
    public void severe(LogType target, String message) {
        logger.log(Level.SEVERE, message);
    }

    @Override
    public void severe(LogType target, String message, Throwable e) {
        logger.log(Level.SEVERE, message, e);
    }

    @Override
    public void warning(LogType target, String message) {
        logger.log(Level.WARNING, message);
    }

    @Override
    public void warning(LogType target, String message, Throwable e) {
        logger.log(Level.WARNING, message, e);
    }

    @Override
    public void info(LogType target, String message) {
        logger.log(Level.INFO, message);
    }

    @Override
    public void info(LogType target, String message, Throwable e) {
        logger.log(Level.INFO, message, e);
    }

    @Override
    public void debug(LogType target, String message) {
        logger.log(Level.FINE, message);
    }

    @Override
    public void debug(LogType target, String message, Throwable e) {
        logger.log(Level.FINE, message, e);
    }
}
