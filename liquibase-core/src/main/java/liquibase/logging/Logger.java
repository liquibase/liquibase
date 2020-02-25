package liquibase.logging;

import liquibase.ExtensibleObject;

import java.util.logging.Level;

/**
 * Interface to class that does the actual logging.
 * Instances will be created by {@link LogService}, normally through {@link liquibase.Scope#getLog(Class)}).
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
     * Log that a severe error that occurred.
     */
    void severe(String message);

    /**
     * Log an error together with data from an error/exception.
     */
    void severe(String message, Throwable e);

    /**
     * Log a event the user should be warned about.
     */
    void warning(String message);

    /**
     * Log a event the user should be warned about together with data from an error/exception.
     */
    void warning(String message, Throwable e);

    /**
     * Logs a general event that might be useful for the user.
     */
    void info(String message);

    /**
     * Logs a general event that might be useful for the user together with data from an error/exception.
     */
    void info(String message, Throwable e);

    /**
     * Logs configuration information.
     */
    void config(String message);

    /**
     * Logs configuration information together with data from an error/exception.
     */
    void config(String message, Throwable e);


    /**
     * Logs a debugging event to aid in troubleshooting.
     */
    void fine(String message);

    /**
     * Logs a debugging event to aid in troubleshooting together with data from an error/exception.
     */
    void fine(String message, Throwable e);

}
