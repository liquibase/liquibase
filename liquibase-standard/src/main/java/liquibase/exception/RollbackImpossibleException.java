package liquibase.exception;

/**
 * Thrown if a change is encountered that cannot be rolled back.
 */
public class RollbackImpossibleException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    
    public RollbackImpossibleException() {
    }

    public RollbackImpossibleException(String message) {
        super(message);
    }

    public RollbackImpossibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public RollbackImpossibleException(Throwable cause) {
        super(cause);
    }
}
