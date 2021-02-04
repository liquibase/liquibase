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
     * Prompt the user with the message and wait for a response.<br>
     * If the user hits "enter" OR this UIService implementation does not support user prompts, return the default value.<br>
     * If inputHandler is null, {@link DefaultInputHandler} will be used.<br>
     * If inputHandler throws an {@link IllegalArgumentException}, the user will be given the chance to re-enter the value.<br>
     * If defaultValue is null, a null value will be returned.
     */
    <T> T prompt(String prompt, T defaultValue, InputHandler<T> inputHandler, Class<T> type);

    /**
     *
     * Method to set flag indicating whether prompting is allowed
     *
     * @param   allowPrompt                 New flag value
     * @throws  IllegalArgumentException    If parameter is not allowed
     *
     */
    void setAllowPrompt(boolean allowPrompt) throws IllegalArgumentException;

    /**
     *
     * Return current setting of allow prompt flag
     *
     * @return   boolean
     *
     */
    boolean getAllowPrompt();
}
