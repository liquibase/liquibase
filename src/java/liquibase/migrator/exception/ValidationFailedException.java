package liquibase.migrator.exception;

import liquibase.migrator.ChangeSet;
import liquibase.migrator.preconditions.FailedPrecondition;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;

public class ValidationFailedException extends MigrationFailedException {
    private List<ChangeSet> invalidMD5Sums;
    private List<FailedPrecondition> failedPreconditions;
    private Set<ChangeSet> duplicateChangeSets;

    public ValidationFailedException(List<ChangeSet> invalidMD5Sums, List<FailedPrecondition> failedPreconditions, Set<ChangeSet> duplicateChangeSets) {
        this.invalidMD5Sums = invalidMD5Sums;
        this.failedPreconditions = failedPreconditions;
        this.duplicateChangeSets = duplicateChangeSets;
    }


    public String getMessage() {
        StringBuffer message = new StringBuffer();
        message.append("Validation Failed:");
        if (invalidMD5Sums.size() > 0) {
            message.append(invalidMD5Sums.size()).append(" change sets failed MD5Sum Check");
        }
        if (failedPreconditions.size() > 0) {
            message.append(failedPreconditions.size()).append(" preconditions failed");
        }
        if (duplicateChangeSets.size() > 0) {
            message.append(duplicateChangeSets.size()).append(" change sets had duplicate identifiers");
        }

        return message.toString();
    }

    public List<ChangeSet> getInvalidMD5Sums() {
        return invalidMD5Sums;
    }

    public void printDescriptiveError(PrintStream out) {
        out.println("Validation Error: ");
        if (invalidMD5Sums.size() > 0) {
            out.println("     "+invalidMD5Sums.size()+" change sets have changed since they were ran against the database");
            for (ChangeSet changeSet : invalidMD5Sums) {
                out.println("          "+changeSet.toString(false));
            }
        }

        if (failedPreconditions.size() > 0) {
            out.println("     "+failedPreconditions.size()+" preconditions failed");
            for (FailedPrecondition failedPrecondition : failedPreconditions) {
                out.println("          "+failedPrecondition.toString());
            }
        }

        if (duplicateChangeSets.size() > 0) {
            out.println("     "+duplicateChangeSets.size()+" change sets had duplicate identifiers");
            for (ChangeSet duplicate : duplicateChangeSets) {
                out.println("          "+duplicate.toString(false));
            }
        }
    }
}
