package liquibase.exception;

public class ChangeLogParseException extends LiquibaseParseException {
    
    private static final long serialVersionUID = 1900592574002703432L;
    
    public ChangeLogParseException(Throwable cause) {
        super(cause);
    }

    public ChangeLogParseException(String message) {
        super(message);
    }

    public ChangeLogParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
