package liquibase.exception;

/**
 * Exception thrown when any pre-execution validation fails.
 */
public class CommandValidationException extends CommandExecutionException {

    public CommandValidationException(String argument, String message) {
        this(argument, message, false);
    }

    public CommandValidationException(String argument, String message, boolean hasInitProjectParameters) {
        super("Invalid argument '" + argument + "': " + message + (hasInitProjectParameters ? ". If you need to configure new liquibase project files and arguments, run the 'liquibase init project' command." : ""));
    }

    public CommandValidationException(String message) {
        super(message);
    }
}
