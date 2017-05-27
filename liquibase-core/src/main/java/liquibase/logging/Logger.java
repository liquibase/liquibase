package liquibase.logging;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.servicelocator.PrioritizedService;

/**
 * Interface for all logger implementations in this software
 */
public interface Logger extends PrioritizedService {

    /**
     * Set the identifier for the logger. LogFactory might use that name to find a logger for a specific purpose.
     * @param name Identifier for the purpose of the logger.
     */
    void setName(String name);

    /**
     * Returns the currently set log level
     * @return the current logging level
     */
    LogLevel getLogLevel();

    /**
     * Changes the current log level
     * @param level the new disired log level
     */
    void setLogLevel(String level);

    /**
     * Changes the current log level
     * @param level the new disired log level
     */
    void setLogLevel(LogLevel level);

    /**
     * Opens a new log file and chooses a new log level at the same time. Typically used to initialize the Logger.
     * @param logLevel desired log level
     * @param logFile
     */
    public void setLogLevel(String logLevel, String logFile);

    /**
     * Informs the logger that work on a new, possible different, DatabaseChangeLog has begun. Might be useful for
     * visualizing the DatabaseChangeLog/ChangeSet hierarchy in the log files.
     * @param databaseChangeLog the new DatabaseChangeLog
     */
    void setChangeLog(DatabaseChangeLog databaseChangeLog);

    /**
     * Informs the logger that work on a new, possible different, ChangeSet has begun. Might be useful for
     * visualizing the DatabaseChangeLog/ChangeSet hierarchy in the log files.
     * @param changeSet the new ChangeSet
     */
    void setChangeSet(ChangeSet changeSet);

    /**
     * Closes the current log output file.
     */
    public void closeLogFile();

    /**
     * Log a severe event.
     * @param message the text message describing the event
     * @see LogLevel
     */
    void severe(String message);

    /**
     * Log a severe event together with data from an error/exception
     * @param message the text message describing the event
     * @param e the error/exception that occured
     * @see LogLevel
     */
    void severe(String message, Throwable e);

    /**
     * Log a event the user should be warned about
     * @param message the text message describing the event
     * @see LogLevel
     */
    void warning(String message);

    /**
     * Log a event the user should be warned about together with data from an error/exception
     * @param message the text message describing the event
     * @param e the error/exception that occured
     * @see LogLevel
     */
    void warning(String message, Throwable e);

    /**
     * Logs a general event that might be useful for the user
     * @param message the text message describing the event
     * @see LogLevel
     */
    void info(String message);

    /**
     * Logs a general event that might be useful for the user together with data from an error/exception
     * @param message the text message describing the event
     * @param e the error/exception that occured
     * @see LogLevel
     */
    void info(String message, Throwable e);

    /**
     * Logs a debugging event to aid in troubleshooting
     * @param message the text message describing the event
     * @see LogLevel
     */
    void debug(String message);

    /**
     * Logs a debugging event to aid in troubleshooting together with data from an error/exception
     * @param message the text message describing the event
     * @param e the error/exception that occured
     * @see LogLevel
     */
    void debug(String message, Throwable e);

}
