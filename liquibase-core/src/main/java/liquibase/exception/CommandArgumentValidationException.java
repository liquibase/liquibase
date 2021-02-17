package liquibase.exception;

public class CommandArgumentValidationException extends CommandExecutionException {

    public CommandArgumentValidationException(String message) {
        super(message);
    }

    public CommandArgumentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
