package liquibase.exception;

/**
 * If there is an error with setting up a Change this Exception
 * will be thrown.
 * 
 * A message must always be provided, if none is then the message
 * from the cause exception will be used.
 *
 */
public class SetupException extends LiquibaseException {

    private static final long serialVersionUID = 1L;

    public SetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public SetupException(String message) {
        super(message);
    }

    public SetupException(Throwable cause) {
        super(cause.getMessage(),cause);
    }

}
