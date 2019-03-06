package liquibase.logging.core;

import liquibase.AbstractExtensibleObject;
import liquibase.logging.LogType;
import liquibase.logging.Logger;

import java.util.Map;
import java.util.logging.Level;

/**
 * Convenience base implementation of a Logger.
 */
public abstract class AbstractLogger extends AbstractExtensibleObject implements Logger {

    @Override
    public void severe(String message) {
        this.severe(LogType.LOG, message);
    }

    @Override
    public void severe(LogType target, String message) {
        this.severe(target, message, null);
    }

    @Override
    public void severe(LogType target, String message, Throwable e) {
        this.log(Level.SEVERE, target, message, e);
    }

    @Override
    public void severe(String message, Throwable e) {
        this.severe(LogType.LOG, message, e);
    }

    @Override
    public void warning(String message) {
        this.warning(LogType.LOG, message);
    }

    @Override
    public void warning(String message, Throwable e) {
        this.warning(LogType.LOG, message, e);
    }

    @Override
    public void warning(LogType target, String message) {
        this.warning(target, message, null);
    }

    @Override
    public void warning(LogType target, String message, Throwable e) {
        this.log(Level.WARNING, target, message, e);
    }

    @Override
    public void info(String message) {
        this.info(LogType.LOG, message);
    }

    @Override
    public void info(String message, Throwable e) {
        this.info(LogType.LOG, message, e);
    }

    @Override
    public void info(LogType logType, String message) {
        this.info(logType, message, null);
    }

    @Override
    public void info(LogType target, String message, Throwable e) {
        this.log(Level.INFO, target, message, e);

    }

    @Override
    public void config(String message) {
        this.config(LogType.LOG, message);
    }

    @Override
    public void config(String message, Throwable e) {
        this.config(LogType.LOG, message, e);
    }

    @Override
    public void config(LogType logType, String message) {
        this.config(logType, message, null);
    }

    @Override
    public void config(LogType target, String message, Throwable e) {
        this.log(Level.CONFIG, target, message, e);
    }

    @Override
    public void fine(String message) {
        this.fine(LogType.LOG, message);
    }

    @Override
    public void fine(String message, Throwable e) {
        this.fine(LogType.LOG, message, e);
    }

    @Override
    public void fine(LogType target, String message) {
        this.fine(target, message, null);
    }

    @Override
    public void fine(LogType target, String message, Throwable e) {
        this.log(Level.FINE, target, message, e);
    }
}
