package liquibase.exception;

public class DuplicateChangeSetException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    
    public DuplicateChangeSetException(String message) {
        super(message);
    }
}
