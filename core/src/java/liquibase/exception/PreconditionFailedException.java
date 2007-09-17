package liquibase.exception;

import liquibase.preconditions.FailedPrecondition;
import liquibase.preconditions.Precondition;
import liquibase.DatabaseChangeLog;

import java.util.List;
import java.util.ArrayList;

/**
 * Thrown when a precondition failed.
 */
public class PreconditionFailedException extends Exception {

    private static final long serialVersionUID = 1L;
    private List<FailedPrecondition> failedPreconditions;

    public PreconditionFailedException(String message, DatabaseChangeLog changeLog, Precondition precondition) {
        this(new FailedPrecondition(message, changeLog, precondition));
    }

    public PreconditionFailedException(FailedPrecondition failedPrecondition) {
        super("Preconditions Failed");
        this.failedPreconditions = new ArrayList<FailedPrecondition>();
        failedPreconditions.add(failedPrecondition);
    }

    public PreconditionFailedException(List<FailedPrecondition> failedPreconditions) {
        super("Preconditions Failed");
        this.failedPreconditions = failedPreconditions;
    }

    public List<FailedPrecondition> getFailedPreconditions() {
        return failedPreconditions;
    }

}