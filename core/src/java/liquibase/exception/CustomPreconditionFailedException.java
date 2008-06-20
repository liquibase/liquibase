package liquibase.exception;

import liquibase.DatabaseChangeLog;
import liquibase.preconditions.FailedPrecondition;
import liquibase.preconditions.Precondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown when a precondition failed.
 */
public class CustomPreconditionFailedException extends Exception {

    private static final long serialVersionUID = 1L;

    public CustomPreconditionFailedException(String message) {
        super(message);
    }

    public CustomPreconditionFailedException(String message, Throwable e) {
        super(message, e);
    }
}