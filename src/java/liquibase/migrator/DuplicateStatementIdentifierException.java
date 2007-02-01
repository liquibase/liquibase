package liquibase.migrator;

public class DuplicateStatementIdentifierException extends Exception {
    public DuplicateStatementIdentifierException(String message) {
        super(message);
    }
}
