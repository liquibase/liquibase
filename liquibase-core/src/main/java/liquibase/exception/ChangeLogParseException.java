package liquibase.exception;

public class ChangeLogParseException extends LiquibaseParseException {

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
