package liquibase.exception;

/**
 * Exception thrown when any pre-execution validation fails.
 */
public class CommandValidationException extends CommandExecutionException {

    public CommandValidationException(String argument, String message) {
        super(buildMessage(argument, message));
    }

    public CommandValidationException(String message) {
        super(message);
    }

    public CommandValidationException(String argument, String message, Throwable cause) {
        super(buildMessage(argument, message), cause);
    }

    private static String buildMessage(String argument, String message) {
        return "Invalid argument '" + argument + "': " + message;
    }
}
