package liquibase.exception;

/**
 * Exception thrown when any pre-execution validation fails.
 */
public class CommandValidationException extends CommandExecutionException {

    public CommandValidationException(String argument, String message) {
        super("Invalid argument '" + argument + "': " + message);
    }

}
