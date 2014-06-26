package liquibase.exception;

/**
 * An UnsupportedException is thrown when an operation is cannot be completed because it is not supported.
 */
public class UnsupportedException extends LiquibaseException {

    public UnsupportedException(String message) {
        super(message);
    }

    public UnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedException(Throwable cause) {
        super(cause);
    }
}
