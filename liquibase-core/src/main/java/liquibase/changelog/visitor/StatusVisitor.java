package liquibase.changelog.visitor;

import liquibase.changelog.*;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.NotInChangeLogChangeSetFilter;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.util.*;

/**
 * ChangeSetVisitor that will collect the execution status of changeSets without executing them. Also includes changeSets
 * previously executed against the database but no longer in the change log.
 */
public class StatusVisitor implements ChangeSetVisitor, SkippedChangeSetVisitor {

    private LinkedHashMap<ChangeSet, ChangeSetStatus> changeSetStatuses = new LinkedHashMap<>();
    private final List<RanChangeSet> ranChangeSets;

    public StatusVisitor(Database database) throws LiquibaseException {
        ranChangeSets = new ArrayList<>(ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getRanChangeSets());
    }

    @Override
    public Direction getDirection() {
        return Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        ChangeSetStatus status = addStatus(changeSet, databaseChangeLog, database);
        status.setWillRun(true);
        status.setFilterResults(filterResults);
    }

    @Override
    public void skipped(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        ChangeSetStatus status = addStatus(changeSet, databaseChangeLog, database);
        status.setWillRun(false);
        status.setFilterResults(filterResults);
    }

    protected ChangeSetStatus addStatus(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) throws LiquibaseException {
        ChangeSetStatus status = new ChangeSetStatus(changeSet);

        RanChangeSet ranChangeSetToRemove = null;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.isSameAs(changeSet)) {
                status.setPreviouslyRan(true);
                status.setDateLastExecuted(ranChangeSet.getDateExecuted());
                status.setStoredCheckSum(ranChangeSet.getLastCheckSum());
                status.setRanChangeSet(ranChangeSet);

                ranChangeSetToRemove = ranChangeSet;

                break;
            }
        }
        if (ranChangeSetToRemove != null) {
            ranChangeSets.remove(ranChangeSetToRemove);
        }

        changeSetStatuses.put(changeSet, status);

        return status;
    }

    /**
     * Convenience method to return the ChangeSetStatus of a given changeSet. Returns null if the changeSet is not know.
     */
    public ChangeSetStatus getStatus(ChangeSet changeSet) {
        return changeSetStatuses.get(changeSet);
    }

    /**
     * Return the status of all changeSets, in the order they exist in the databasechangelog.
     * Any change sets not in the current change log but previously ran against the database will be at the front of the List
     * with a not run reason type of {@link liquibase.changelog.filter.NotInChangeLogChangeSetFilter}
     */
    public List<ChangeSetStatus> getStatuses() {
        ArrayList<ChangeSetStatus> returnList = new ArrayList<>();
        for (RanChangeSet changeSet : ranChangeSets) {
            ChangeSetStatus status = new ChangeSetStatus(new ChangeSet(changeSet.getId(), changeSet.getAuthor(), false, false, changeSet.getChangeLog(), null, null, null));
            status.setPreviouslyRan(true);
            status.setDateLastExecuted(changeSet.getDateExecuted());
            status.setStoredCheckSum(changeSet.getLastCheckSum());

            status.setComments(changeSet.getComments());
            status.setDescription(changeSet.getDescription());
            status.setWillRun(false);
            status.setFilterResults(new HashSet<>(Arrays.asList(new ChangeSetFilterResult(false, "Change set is not in change log", NotInChangeLogChangeSetFilter.class))));
            status.setRanChangeSet(changeSet);

            returnList.add(status);
        }

        returnList.addAll(changeSetStatuses.values());

        return returnList;
    }

    /**
     * Return the change sets that will execute
     */
    public List<ChangeSetStatus> getChangeSetsToRun() {
        ArrayList<ChangeSetStatus> returnList = new ArrayList<>();
        for (ChangeSetStatus status : changeSetStatuses.values()) {
            if (status.getWillRun()) {
                returnList.add(status);
            }
        }

        return returnList;
    }

    /**
     * Return the change sets that will NOT execute
     */
    public List<ChangeSetStatus> getChangeSetsToSkip() {
        ArrayList<ChangeSetStatus> returnList = new ArrayList<>();
        for (ChangeSetStatus status : changeSetStatuses.values()) {
            if (!status.getWillRun()) {
                returnList.add(status);
            }
        }

        return returnList;
    }
}
