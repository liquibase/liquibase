package liquibase.migrator;

/**
 * Thrown if a change is encountered that cannot be rolled back.
 */
public class RollbackImpossibleException extends Exception {
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
