package liquibase.snapshot;

import liquibase.exception.LiquibaseException;

/**
 * Thrown if a descriptive example is not specific enough to perform a snapshot.
 */
public class InvalidExampleException extends LiquibaseException {
    private static final long serialVersionUID = -9048846580103821702L;
    
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
