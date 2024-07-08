package liquibase.exception;

/**
 * Exception thrown when any pre-execution validation fails.
 */
public class CommandValidationException extends CommandExecutionException {


    private static final long serialVersionUID = 7679286738030355515L;

    public CommandValidationException(String argument, String message) {
        super(buildMessage(argument, message));
    }

    public CommandValidationException(String message) {
        super(message);
    }

    public CommandValidationException(Throwable cause) {
        super(cause);
    }

    public CommandValidationException(String argument, String message, Throwable cause) {
        super(buildMessage(argument, message), cause);
    }

    public CommandValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    private static String buildMessage(String argument, String message) {
        return "Invalid argument '" + argument + "': " + message;
    }
}
