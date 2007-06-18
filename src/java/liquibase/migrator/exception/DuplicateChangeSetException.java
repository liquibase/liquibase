package liquibase.migrator.exception;

public class DuplicateChangeSetException extends Exception {
    public DuplicateChangeSetException(String message) {
        super(message);
    }
}
