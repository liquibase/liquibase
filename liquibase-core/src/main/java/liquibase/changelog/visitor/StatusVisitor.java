package liquibase.changelog.visitor;

import liquibase.change.CheckSum;
import liquibase.changelog.*;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.NotInChangeLogChangeSetFilter;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.util.*;

public class StatusVisitor implements ChangeSetVisitor, SkippedChangeSetVisitor {

    private LinkedHashMap<ChangeSet, ChangeSetStatus> changeSetStatuses = new LinkedHashMap<ChangeSet, ChangeSetStatus>();
    private final List<RanChangeSet> ranChangeSets;

    public StatusVisitor(Database database) throws LiquibaseException {
        ranChangeSets = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getRanChangeSets();
    }

    @Override
    public Direction getDirection() {
        return Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        ChangeSetStatus status = addStatus(changeSet, databaseChangeLog, database);
        status.setWillRun(true);
        status.setRunReasons(filterResults);
    }

    @Override
    public void skipped(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSetFilterResult filterResult) throws LiquibaseException {
        ChangeSetStatus status = addStatus(changeSet, databaseChangeLog, database);
        status.setWillRun(false);
        status.setSkipReason(filterResult);
    }

    protected ChangeSetStatus addStatus(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) throws LiquibaseException {
        ChangeSetStatus status = new ChangeSetStatus(changeSet);

        RanChangeSet ranChangeSetToRemove = null;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.isSameAs(changeSet)) {
                status.setPreviouslyRan(true);
                status.setDateLastExecuted(ranChangeSet.getDateExecuted());
                status.setStoredCheckSum(ranChangeSet.getLastCheckSum());

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

    public ChangeSetStatus getStatus(ChangeSet changeSet) {
        return changeSetStatuses.get(changeSet);
    }

    public List<ChangeSetStatus> getStatuses() {
        ArrayList<ChangeSetStatus> returnList = new ArrayList<ChangeSetStatus>();
        for (RanChangeSet changeSet : ranChangeSets) {
            ChangeSetStatus status = new ChangeSetStatus(new ChangeSet(changeSet.getId(), changeSet.getAuthor(), false, false, changeSet.getChangeLog(), null, null, null));
            status.setPreviouslyRan(true);
            status.setDateLastExecuted(changeSet.getDateExecuted());
            status.setStoredCheckSum(changeSet.getLastCheckSum());

            status.setComments(changeSet.getComments());
            status.setDescription(changeSet.getDescription());
            status.setWillRun(false);
            status.setSkipReason(new ChangeSetFilterResult(false, "Change set is not in change log", NotInChangeLogChangeSetFilter.class));

            returnList.add(status);
        }

        returnList.addAll(changeSetStatuses.values());

        return returnList;
    }

    public List<ChangeSetStatus> getChangeSetsToRun() {
        ArrayList<ChangeSetStatus> returnList = new ArrayList<ChangeSetStatus>();
        for (ChangeSetStatus status : changeSetStatuses.values()) {
            if (status.getWillRun()) {
                returnList.add(status);
            }
        }

        return returnList;
    }

    public List<ChangeSetStatus> getChangeSetsToSkip() {
        ArrayList<ChangeSetStatus> returnList = new ArrayList<ChangeSetStatus>();
        for (ChangeSetStatus status : changeSetStatuses.values()) {
            if (!status.getWillRun()) {
                returnList.add(status);
            }
        }

        return returnList;
    }
}
