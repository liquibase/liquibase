package liquibase.exception;

public class UnsupportedChangeException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    
    public UnsupportedChangeException(String message) {
        super(message);
    }

    public UnsupportedChangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedChangeException(Throwable cause) {
        super(cause);
    }
}
