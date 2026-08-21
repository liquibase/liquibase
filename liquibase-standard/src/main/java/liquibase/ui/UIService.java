package liquibase.ui;

import liquibase.ExtensibleObject;
import liquibase.plugin.Plugin;

/**
 * Service for interacting with the user.
 */
public interface UIService extends ExtensibleObject, Plugin {

    /**
     * Semantic type for a message, allowing UI implementations to apply
     * presentation-layer treatment (e.g. colour) without the caller needing
     * to know how or whether the terminal supports it.
     *
     * <p>The set of values is intentionally small.  Only distinct visual
     * outcomes that are broadly useful across all CLI commands are listed here.
     * A plain/untyped message should use {@link #sendMessage(String)} directly.
     *
     * @since 5.2
     */
    enum MessageType {
        /**
         * A command completed without errors.  Rendered green when the
         * terminal supports colour and colour output is enabled.
         */
        SUCCESS
    }

    int getPriority();

    /**
     * Send a "normal" message to the user.
     */
    void sendMessage(String message);

    /**
     * Send a typed message to the user.
     *
     * <p>Implementations that support coloured or otherwise styled output may
     * use the {@code type} hint to apply the appropriate presentation.  The
     * default implementation simply delegates to {@link #sendMessage(String)}
     * so that existing {@link UIService} implementations are unaffected.
     *
     * @param message the text to display
     * @param type    the semantic classification of the message
     * @since 5.2
     */
    default void sendMessage(String message, MessageType type) {
        sendMessage(message);
    }

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
     * If this UIService implementation does not support user prompts, return the default value.<br>
     * If inputHandler is null, {@link DefaultInputHandler} will be used.<br>
     * If inputHandler throws an {@link IllegalArgumentException}, the user will be given the chance to re-enter the value.<br>
     * If the inputHandler returns true for {@link InputHandler#shouldAllowEmptyInput()} and the user enters an empty value
     * when prompted, or hits "enter", the valueIfNoEntry will be returned. If the inputHandler returns false for
     * {@link InputHandler#shouldAllowEmptyInput()}, the user will be reprompted until they enter a non-empty value,
     * which will then be returned.
     */
    <T> T prompt(String prompt, T valueIfNoEntry, InputHandler<T> inputHandler, Class<T> type);

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
