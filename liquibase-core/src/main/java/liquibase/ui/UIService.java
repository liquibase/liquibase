package liquibase.ui;

import liquibase.ExtensibleObject;
import liquibase.exception.LiquibaseException;
import liquibase.plugin.Plugin;

/**
 * Service for interacting with the user.
 */
public interface UIService extends ExtensibleObject, Plugin {

    int getPriority();

    /**
     * Send a "normal" message to the user.
     */
    void sendMessage(String message);

    /**
     * Send an "error" message to the user.
     */
    void sendErrorMessage(String message);

    /**
     * Send an "error" message to the user along with a stacktrace.
     */
    void sendErrorMessage(String message, Throwable exception);

    /**
     *
     * Prompt the user with the message and wait with a running time
     * with a running time.  Return the response as a String
     *
     */
    <T> T prompt(String promptString, T defaultValue, int timerValue, Class<T> type) throws LiquibaseException;

}
