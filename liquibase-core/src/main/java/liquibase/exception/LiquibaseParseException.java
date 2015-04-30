package liquibase.exception;

public class LiquibaseParseException extends LiquibaseException {

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
