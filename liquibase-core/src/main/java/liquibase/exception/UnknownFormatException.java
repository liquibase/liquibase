package liquibase.exception;

/**
 * Thrown by SnapshotParserFactory if it cannot find a parser for a given file format.
 */
public class UnknownFormatException extends LiquibaseException {
    public UnknownFormatException() {
    }

    public UnknownFormatException(String message) {
        super(message);
    }

    public UnknownFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownFormatException(Throwable cause) {
        super(cause);
    }
}
