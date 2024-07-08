package liquibase.exception;

public class DuplicateStatementIdentifierException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    
    public DuplicateStatementIdentifierException(String message) {
        super(message);
    }
}
