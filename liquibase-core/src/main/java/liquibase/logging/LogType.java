package liquibase.logging;

/**
 * The type of message being logged.
 */
public enum LogType {

    /**
     * A message that should always be shown in a user interface (like the CLI)
     */
    USER_MESSAGE,

    /**
     * An SQL statement that makes no changes to the database.
     */
    READ_SQL,

    /**
     * An SQL statement that modified the state of the database.
     */
    WRITE_SQL,

    /**
     * An message that can just be sent to a log file.
     */
    LOG
}
