package liquibase.exception;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.FailedPrecondition;

import java.util.ArrayList;
import java.util.List;

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