package liquibase.migrator.exception;

/**
 * Thrown when a precondition failed.
 */
public class PreconditionFailedException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public PreconditionFailedException() {
    }

    public PreconditionFailedException(String message) {
        super(message);
    }

    public PreconditionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreconditionFailedException(Throwable cause) {
        super(cause);
    }
}