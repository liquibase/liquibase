package liquibase.logging;

/**
 * Interface for all logger implementations in this software. The hierarchy of log levels is:
 * (finest) DEBUG < SQL < INFO < WARNING < ERROR (coarsest)
 */
public interface Logger {

    /**
     * Returns the currently set log level
     * @return the current logging level
     */
    LogLevel getLogLevel();

//    /**
//     * Closes the current log output file.
//     */
//    void closeLogFile();

    /**
     * Log a severe event.
     *
     * @param target
     * @param message the text message describing the event
     * @see LogLevel
     */
    void error(LogTarget target, String message);

    /**
     * Log a severe event together with data from an error/exception
     * @param target
     * @param message the text message describing the event
     * @param e the error/exception that occured
     * @see LogLevel
     */
    void error(LogTarget target, String message, Throwable e);

    /**
     * Log a event the user should be warned about
     * @param target
     * @param message the text message describing the event
     * @see LogLevel
     */
    void warn(LogTarget target, String message);

    /**
     * Log a event the user should be warned about together with data from an error/exception
     * @param target
     * @param message the text message describing the event
     * @param e the error/exception that occured
     * @see LogLevel
     */
    void warn(LogTarget target, String message, Throwable e);

    /**
     * Logs a general event that might be useful for the user
     *
     * @param logTarget
     * @param message the text message describing the event
     * @see LogLevel
     */
    void info(LogTarget logTarget, String message);

    /**
     * Logs a general event that might be useful for the user together with data from an error/exception
     *
     * @param target
     * @param message the text message describing the event
     * @param e the error/exception that occured
     * @see LogLevel
     */
    void info(LogTarget target, String message, Throwable e);

    /**
     * Logs a native SQL statement sent to a database instance
     *
     * @param target
     * @param message the text message describing the event
     * @see LogLevel
     */
    void sql(LogTarget target, String message);

    /**
     * Logs a debugging event to aid in troubleshooting
     *
     * @param target
     * @param message the text message describing the event
     * @see LogLevel
     */
    void debug(LogTarget target, String message);

    /**
     * Logs a debugging event to aid in troubleshooting together with data from an error/exception
     * @param target
     * @param message the text message describing the event
     * @param e the error/exception that occured  @see LogLevel
     */
    void debug(LogTarget target, String message, Throwable e);

}
