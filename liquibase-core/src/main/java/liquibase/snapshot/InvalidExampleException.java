package liquibase.snapshot;

import liquibase.exception.LiquibaseException;

/**
 * Thrown if a descriptive example is not specific enough to perform a snapshot.
 */
public class InvalidExampleException extends LiquibaseException {
    public InvalidExampleException() {
    }

    public InvalidExampleException(String message) {
        super(message);
    }

    public InvalidExampleException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidExampleException(Throwable cause) {
        super(cause);
    }
}
