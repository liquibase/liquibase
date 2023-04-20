package liquibase.exception;

public class LiquibaseParseException extends LiquibaseException {
    
    private static final long serialVersionUID = 5849916948489798893L;
    
    public LiquibaseParseException() {
    }

    public LiquibaseParseException(String message) {
        super(message);
    }

    public LiquibaseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseParseException(Throwable cause) {
        super(cause);
    }
}
