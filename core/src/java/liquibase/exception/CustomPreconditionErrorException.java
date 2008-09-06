package liquibase.exception;

import liquibase.DatabaseChangeLog;
import liquibase.preconditions.FailedPrecondition;
import liquibase.preconditions.Precondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown when a precondition failed.
 */
public class CustomPreconditionErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    public CustomPreconditionErrorException(String message) {
        super(message);
    }

    public CustomPreconditionErrorException(String message, Throwable e) {
        super(message, e);
    }
}