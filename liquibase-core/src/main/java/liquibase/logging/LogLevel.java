package liquibase.logging;

/**
 * The allowed levels for logging. The hierarchy is defined as following:
 * "<" means "returns fewer information than"
 * OFF < ERROR < WARNING < INFO < DEBUG
 */
public enum LogLevel {


    TRACE,
    /**
     * Very detailed information about every internal step this program takes. Normally only activated during
     * troubleshooting.
     */
    DEBUG,
    /*
     * A native SQL statement sent to a database instance
     */
    SQL,
    /**
     * Regular message of a successful completion of a working unit, or output of configuration
     * information that is expected to be essential for troubleshooting.
     */
    INFO,
    /**
     * An event that allows further processing, but should caution the user to have a thorough look at
     * the log message.
     */
    WARNING,
    /**
     * Errors and other severe events. In most cases, these events will stop further processing of a change log.
     */
    ERROR,
/**
     * No logging at all
     */
    OFF

}
