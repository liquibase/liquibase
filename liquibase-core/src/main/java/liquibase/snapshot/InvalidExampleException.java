package liquibase.snapshot;

import liquibase.exception.LiquibaseException;

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
