package liquibase.logging;

/**
 * Interface to class that does the actual logging.
 * Instances will be created by {@link LoggerFactory} to know the class to log against.
 *
 * All log methods take a {@link LogType} to describe the type of message being logged.
 *
 * The hierarchy of log levels is:
 * (finest) DEBUG < INFO < WARN < ERROR (coarsest)
 */
public interface Logger {

    /**
     * Log an error that occurred.
     */
    void error(LogType target, String message);

    /**
     * Log an error together with data from an error/exception
     */
    void error(LogType target, String message, Throwable e);

    /**
     * Log a event the user should be warned about
     */
    void warn(LogType target, String message);

    /**
     * Log a event the user should be warned about together with data from an error/exception
     */
    void warn(LogType target, String message, Throwable e);

    /**
     * Logs a general event that might be useful for the user.
     */
    void info(LogType logType, String message);

    /**
     * Logs a general event that might be useful for the user together with data from an error/exception
     */
    void info(LogType target, String message, Throwable e);

    /**
     * Logs a debugging event to aid in troubleshooting
     */
    void debug(LogType target, String message);

    /**
     * Logs a debugging event to aid in troubleshooting together with data from an error/exception
     */
    void debug(LogType target, String message, Throwable e);

}
