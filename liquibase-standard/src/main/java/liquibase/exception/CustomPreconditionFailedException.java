package liquibase.exception;

/**
 * Thrown when a precondition failed.
 */
public class CustomPreconditionFailedException extends Exception {

    private static final long serialVersionUID = 1L;

    public CustomPreconditionFailedException(String message) {
        super(message);
    }

    public CustomPreconditionFailedException(String message, Throwable e) {
        super(message, e);
    }
}