package liquibase.logging;

/**
 * Filter to apply to messages before they are sent to underlying logging systems.
 * Usually used to remove potentially insecure data
 */
public interface LogMessageFilter {

    String filterMessage(String message);

}
