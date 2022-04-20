package liquibase.logging;

/**
 * @deprecated No longer used for filtering log messages.
 * Log messages should be filtered for sensitive information before sending them to the log.
 */
public interface LogMessageFilter {

    String filterMessage(String message);

}
