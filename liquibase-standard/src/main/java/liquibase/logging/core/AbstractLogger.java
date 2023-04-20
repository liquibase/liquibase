package liquibase.logging.core;

import liquibase.AbstractExtensibleObject;
import liquibase.logging.LogMessageFilter;
import liquibase.logging.Logger;

import java.util.logging.Level;

/**
 * Convenience base implementation of a Logger.
 * Default implementation calls down to the {@link #log(Level, String, Throwable)} method for all the convenience methods.
 */
public abstract class AbstractLogger extends AbstractExtensibleObject implements Logger {


    /**
     * @deprecated use {@link AbstractLogger().
     * Passed filter is not used.
     */
    @Deprecated
    protected AbstractLogger(LogMessageFilter ignored) {
    }

    protected AbstractLogger() {
    }

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

    @Override
    public void debug(String message) {
        this.fine(message);
    }

    @Override
    public void debug(String message, Throwable e) {
        this.fine(message, e);
    }

    /**
     * @deprecated now just returns the message
     */
    @Deprecated
    protected String filterMessage(String message) {
        return message;
    }
}
