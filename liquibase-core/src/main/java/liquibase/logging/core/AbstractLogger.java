package liquibase.logging.core;

import liquibase.AbstractExtensibleObject;
import liquibase.logging.Logger;

import java.util.logging.Level;

/**
 * Convenience base implementation of a Logger.
 */
public abstract class AbstractLogger extends AbstractExtensibleObject implements Logger {

    @Override
    public void severe(String message) {
        this.severe(message, null);
    }

    @Override
    public void severe(String message, Throwable e) {
        this.log(Level.SEVERE, message, e);
    }

    @Override
    public void warning(String message) {
        this.warning(message, null);
    }

    @Override
    public void warning(String message, Throwable e) {
        this.log(Level.WARNING, message, e);
    }

    @Override
    public void info(String message) {
        this.info(message, null);
    }

    @Override
    public void info(String message, Throwable e) {
        this.log(Level.INFO, message, e);
    }

    @Override
    public void config(String message) {
        this.config(message, null);
    }

    @Override
    public void config(String message, Throwable e) {
        this.log(Level.CONFIG, message, e);
    }

    @Override
    public void fine(String message) {
        this.fine(message, null);
    }

    @Override
    public void fine(String message, Throwable e) {
        this.log(Level.FINE, message, e);
    }
}
