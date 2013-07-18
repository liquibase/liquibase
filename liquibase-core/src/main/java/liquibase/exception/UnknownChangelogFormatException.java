package liquibase.exception;

public class UnknownChangelogFormatException extends LiquibaseException {
    public UnknownChangelogFormatException() {
    }

    public UnknownChangelogFormatException(String message) {
        super(message);
    }

    public UnknownChangelogFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownChangelogFormatException(Throwable cause) {
        super(cause);
    }
}
