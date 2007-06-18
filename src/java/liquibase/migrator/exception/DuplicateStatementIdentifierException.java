package liquibase.migrator.exception;

public class DuplicateStatementIdentifierException extends Exception {
    public DuplicateStatementIdentifierException(String message) {
        super(message);
    }
}
