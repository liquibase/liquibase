package liquibase.changelog.visitor;

import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.RanChangeSet;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.preconditions.AndPrecondition;
import liquibase.preconditions.ErrorPrecondition;
import liquibase.preconditions.FailedPrecondition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidatingVisitor implements ChangeSetVisitor {

    private List<ChangeSet> invalidMD5Sums = new ArrayList<ChangeSet>();
    private List<FailedPrecondition> failedPreconditions = new ArrayList<FailedPrecondition>();
    private List<ErrorPrecondition> errorPreconditions = new ArrayList<ErrorPrecondition>();
    private Set<ChangeSet> duplicateChangeSets = new HashSet<ChangeSet>();
    private List<SetupException> setupExceptions = new ArrayList<SetupException>();
    private List<Throwable> changeValidationExceptions = new ArrayList<Throwable>();

    private Set<String> seenChangeSets = new HashSet<String>();

    private List<RanChangeSet> ranChangeSets;

    public ValidatingVisitor(List<RanChangeSet> ranChangeSets) {
        this.ranChangeSets = ranChangeSets;
    }

    public void validate(Database database, DatabaseChangeLog changeLog) {
        try {
            AndPrecondition precondition = changeLog.getPreconditions();
            if (precondition == null) {
                return;
            }
            precondition.check(database, changeLog);
        } catch (PreconditionFailedException e) {
            failedPreconditions.addAll(e.getFailedPreconditions());
        } catch (PreconditionErrorException e) {
            errorPreconditions.addAll(e.getErrorPreconditions());
        }
    }

    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    public void visit(ChangeSet changeSet, Database database) {
        for (Change change : changeSet.getChanges()) {
            try {
                change.setUp();
            } catch (SetupException se) {
                setupExceptions.add(se);
            }

            try {
                change.validate(database);
            } catch (Throwable e) {
                changeValidationExceptions.add(e);
            }
        }

        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.getId().equals(changeSet.getId())
                    && ranChangeSet.getAuthor().equals(changeSet.getAuthor())
                    && ranChangeSet.getChangeLog().equals(changeSet.getFilePath())) {
                if (!changeSet.isCheckSumValid(ranChangeSet.getMd5sum())) {
                    if (!changeSet.shouldRunOnChange()) {
                        invalidMD5Sums.add(changeSet);
                    }
                }
            }
        }


        String changeSetString = changeSet.toString(false);
        if (seenChangeSets.contains(changeSetString)) {
            duplicateChangeSets.add(changeSet);
        } else {
            seenChangeSets.add(changeSetString);
        }
    }

    public List<ChangeSet> getInvalidMD5Sums() {
        return invalidMD5Sums;
    }


    public List<FailedPrecondition> getFailedPreconditions() {
        return failedPreconditions;
    }

    public List<ErrorPrecondition> getErrorPreconditions() {
        return errorPreconditions;
    }

    public Set<ChangeSet> getDuplicateChangeSets() {
        return duplicateChangeSets;
    }

    public List<SetupException> getSetupExceptions() {
        return setupExceptions;
    }

    public List<Throwable> getChangeValidationExceptions() {
        return changeValidationExceptions;
    }

    public boolean validationPassed() {
        return invalidMD5Sums.size() == 0
                && failedPreconditions.size() == 0
                && errorPreconditions.size() == 0
                && duplicateChangeSets.size() == 0
                && changeValidationExceptions.size() == 0
                && setupExceptions.size() == 0;
    }

}
