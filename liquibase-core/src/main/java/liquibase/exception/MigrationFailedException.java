package liquibase.exception;

import liquibase.changelog.ChangeSet;

import java.util.LinkedList;
import java.util.List;

public class MigrationFailedException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    private ChangeSet failedChangeSet;
    private List<ChangeSet> failedChangeSets = null;

    public MigrationFailedException() {}

    public MigrationFailedException(ChangeSet failedChangeSet, String message) {
        super(message);
        this.failedChangeSet = failedChangeSet;
        this.failedChangeSets = filterFailedChangeSets(failedChangeSet);
    }

    public MigrationFailedException(ChangeSet failedChangeSet, String message, Throwable cause) {
        super(message, cause);
        this.failedChangeSet = failedChangeSet;
        this.failedChangeSets = filterFailedChangeSets(failedChangeSet);
    }

    public MigrationFailedException(ChangeSet failedChangeSet, Throwable cause) {
        super(cause);
        this.failedChangeSet = failedChangeSet;
        this.failedChangeSets = filterFailedChangeSets(failedChangeSet);
    }

    /**
     *
     * @return true if any changeSets were deployed before {@link MigrationFailedException} was thrown.
     */
    public boolean containsDeployedChangeLogs() {
        return !(failedChangeSets == null || failedChangeSets.isEmpty());
    }

    /**
     *
     * @return all changeSets that were deployed before {@link MigrationFailedException} was thrown.
     */
    public List<ChangeSet> getDeployedChangeLogs() {
        return new LinkedList<>(failedChangeSets);
    }

    /**
     *
     * @return a changeSet that caused the {@link MigrationFailedException}
     */
    public ChangeSet getFailedChangeSet() {
        return failedChangeSet;
    }

    /**
     *
     * @param firstFailed first changeSet from sequence that was a reason for exception.
     * @return a list of {@link ChangeSet} that were deployed before exception fired.
     */
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
        final String failedChangeSetName = failedChangeSet == null ? "(unknown)" : failedChangeSet.toString(false);

        String message = "Migration failed";
        if (failedChangeSetName != null) {
            message += " for changeset " + failedChangeSetName;
        }
        message += ":\n     Reason: " + super.getMessage();

        return message;
    }
}
