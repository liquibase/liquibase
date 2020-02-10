package liquibase.logging;

import liquibase.ExtensibleObject;

import java.util.logging.Level;

/**
 * Interface to class that does the actual logging.
 * Instances will be created by {@link LogService}, normally through {@link liquibase.Scope#getLog(Class)}).
 *
 * All log methods take a {@link LogType} to describe the type of message being logged.
 */
public interface Logger extends ExtensibleObject, AutoCloseable {

    @Override
    default void close() throws Exception {

    }

    /**
     * Generic log method that can log at any log level
     */
    void log(Level level, String message, Throwable e);


    /**
     * Log an error that occurred, using the {@link LogType#LOG} type.
     */
    void severe(String message);

    /**
     * Log an error together with data from an error/exception, using the {@link LogType#LOG} type.
     */
    void severe(String message, Throwable e);

    /**
     * Log a event the user should be warned about, using the {@link LogType#LOG} type.
     */
    void warning(String message);

    /**
     * Log a event the user should be warned about together with data from an error/exception, using the {@link LogType#LOG} type.
     */
    void warning(String message, Throwable e);

    /**
     * Logs a general event that might be useful for the user, using the {@link LogType#LOG} type.
     */
    void info(String message);

    /**
     * Logs a general event that might be useful for the user together with data from an error/exception, using the {@link LogType#LOG} type.
     */
    void info(String message, Throwable e);

    /**
     * Logs configuration information, using the {@link LogType#LOG} type.
     */
    void config(String message);

    /**
     * Logs configuration information together with data from an error/exception, using the {@link LogType#LOG} type.
     */
    void config(String message, Throwable e);


    /**
     * Logs a debugging event to aid in troubleshooting, using the {@link LogType#LOG} type.
     */
    void fine(String message);

    /**
     * Logs a debugging event to aid in troubleshooting together with data from an error/exception, using the {@link LogType#LOG} type.
     */
    void fine(String message, Throwable e);

}
