package liquibase.migrator.exception;

public class RollbackFailedException extends Exception {
    public RollbackFailedException() {
    }

    public RollbackFailedException(String message) {
        super(message);
    }

    public RollbackFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RollbackFailedException(Throwable cause) {
        super(cause);
    }
}
