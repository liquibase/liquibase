package liquibase.migrator;

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
