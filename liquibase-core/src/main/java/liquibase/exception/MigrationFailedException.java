package liquibase.exception;

import liquibase.changelog.ChangeSet;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MigrationFailedException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    private final String failedChangeSetName;
    private List<ChangeSet> failedChangeSets = null;
    
    public MigrationFailedException() {
        failedChangeSetName = "(unknown)";
    }

    public MigrationFailedException(ChangeSet failedChangeSet, String message) {
        super(message);
        this.failedChangeSetName = failedChangeSet.toString(false);
        this.failedChangeSets = filterFailedChangeSets(failedChangeSet);
    }


    public MigrationFailedException(ChangeSet failedChangeSet, String message, Throwable cause) {
        super(message, cause);
        this.failedChangeSetName = failedChangeSet.toString(false);
        this.failedChangeSets = filterFailedChangeSets(failedChangeSet);
    }

    public MigrationFailedException(ChangeSet failedChangeSet, Throwable cause) {
        super(cause);
        this.failedChangeSetName = failedChangeSet.toString(false);
        this.failedChangeSets = filterFailedChangeSets(failedChangeSet);
    }

    public boolean containsFailedChangeset() {
        return !(failedChangeSets == null || failedChangeSets.isEmpty());
    }

    public List<ChangeSet> getFailedChangeSets() {
        return new LinkedList<>(failedChangeSets);
    }

    private List<ChangeSet> filterFailedChangeSets(ChangeSet firstFailed) {
        final List<ChangeSet> allChangeSetsFromChangeLog = firstFailed.getChangeLog().getChangeSets();
        final List<ChangeSet> allChangeSetsBeforeFailedOne = new LinkedList<>();
        for (ChangeSet changeSet : allChangeSetsFromChangeLog) {
            if (changeSet.getId().equals(firstFailed.getId())) {
                break;
            }
            allChangeSetsBeforeFailedOne.add(changeSet);
        }
        return allChangeSetsBeforeFailedOne;
    }

    @Override
    public String getMessage() {
        String message = "Migration failed";
        if (failedChangeSetName != null) {
            message += " for changeset "+ failedChangeSetName;
        }
        message += ":\n     Reason: "+super.getMessage();

        return message;
    }
}
