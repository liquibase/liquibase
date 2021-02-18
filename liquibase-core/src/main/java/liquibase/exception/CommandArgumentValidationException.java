package liquibase.exception;

public class CommandArgumentValidationException extends CommandExecutionException {

    public CommandArgumentValidationException(String argument, String message) {
        super("Invalid argument '" + argument + "': " + message);
    }

}
