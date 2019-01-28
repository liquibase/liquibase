package liquibase.logging.core;

import liquibase.AbstractExtensibleObject;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.plugin.AbstractPlugin;

/**
 * Convenience base implementation of a Logger.
 */
public abstract class AbstractLogger extends AbstractExtensibleObject implements Logger {

    @Override
    public void severe(String message) {
        this.severe(LogType.LOG, message);
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
    public void info(String message) {
        this.info(LogType.LOG, message);
    }

    @Override
    public void info(String message, Throwable e) {
        this.info(LogType.LOG, message, e);
    }

    @Override
    public void debug(String message) {
        this.debug(LogType.LOG, message);
    }

    @Override
    public void debug(String message, Throwable e) {
        this.debug(LogType.LOG, message, e);
    }
}
