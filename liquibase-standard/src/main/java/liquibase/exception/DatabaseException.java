package liquibase.exception;

public class DatabaseException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    
    public DatabaseException() {
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
