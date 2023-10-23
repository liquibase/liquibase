package liquibase.changelog;

import liquibase.ChecksumVersion;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public abstract class AbstractChangeLogHistoryService implements ChangeLogHistoryService {

    private Database database;
    private String deploymentId;

    public Database getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public void reset() {

    }

    @Override
    public ChangeSet.RunStatus getRunStatus(final ChangeSet changeSet)
            throws DatabaseException, DatabaseHistoryException {
        RanChangeSet foundRan = getRanChangeSet(changeSet);

        if (foundRan == null) {
            return ChangeSet.RunStatus.NOT_RAN;
        } else {
            if (foundRan.getLastCheckSum() == null) {
                try {
                    Scope.getCurrentScope().getLog(getClass()).info("Updating NULL md5sum for " + changeSet.toString());
                    replaceChecksum(changeSet);
                } catch (DatabaseException e) {
                    throw new DatabaseException(e);
                }

                return ChangeSet.RunStatus.ALREADY_RAN;
            } else {
                if (foundRan.getLastCheckSum().equals(changeSet.generateCheckSum(
                        ChecksumVersion.enumFromChecksumVersion(foundRan.getLastCheckSum().getVersion())))) {
                    return ChangeSet.RunStatus.ALREADY_RAN;
                } else {
                    if (changeSet.shouldRunOnChange()) {
                        return ChangeSet.RunStatus.RUN_AGAIN;
                    } else {
                        return ChangeSet.RunStatus.INVALID_MD5SUM;
                    }
                }
            }
        }
    }

    @Override
    public void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts,
                                 LabelExpression labels) throws DatabaseException {
        for (RanChangeSet ranChangeSet : this.getRanChangeSets()) {
            if (ranChangeSet.getLastCheckSum() == null) {
                List<ChangeSet> changeSets = databaseChangeLog.getChangeSets(ranChangeSet);
                for (ChangeSet changeSet : changeSets) {
                    if ((changeSet != null) && new ContextChangeSetFilter(contexts).accepts(changeSet).isAccepted() &&
                            new DbmsChangeSetFilter(getDatabase()).accepts(changeSet).isAccepted()) {
                        Scope.getCurrentScope().getLog(getClass()).fine(
                                "Updating null or out of date checksum on changeSet " + changeSet + " to correct value"
                        );
                        replaceChecksum(changeSet);
                    }
                }
            }
        }
    }

    @Override
    public RanChangeSet getRanChangeSet(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        for (RanChangeSet ranChange : getRanChangeSets()) {
            if (ranChange.isSameAs(changeSet)) {
                return ranChange;
            }
        }
        return null;
    }

    @Override
    public Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        RanChangeSet ranChange = getRanChangeSet(changeSet);
        if (ranChange == null) {
            return null;
        } else {
            return ranChange.getDateExecuted();
        }
    }

    /**
     * Returns the deployment ID of the last changeset that has been run, or {@code null} if no changesets have been run yet.
     *
     * @return the deployment ID of the last changeset that has been run, or null if no changesets have been run yet.
     * @throws DatabaseException if there is an error accessing the database
     */
     public String getLastDeploymentId() throws DatabaseException {
         List<RanChangeSet> ranChangeSetsList = getRanChangeSets();
         if (ranChangeSetsList == null || ranChangeSetsList.isEmpty()) {
             return null;
         }
         RanChangeSet lastRanChangeSet = ranChangeSetsList.get(ranChangeSetsList.size() - 1);
         return lastRanChangeSet.getDeploymentId();
    }

    @Override
    public void replaceChecksum(ChangeSet changeSet) throws DatabaseException {
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase()).execute(new UpdateChangeSetChecksumStatement
                (changeSet));
        getDatabase().commit();
        reset();
    }

    @Override
    public String getDeploymentId() {
        return this.deploymentId;
    }

    @Override
    public void resetDeploymentId() {
        this.deploymentId = null;
    }

    @Override
    public void generateDeploymentId() {
        if (this.deploymentId == null) {
            long time = (new Date()).getTime();
            String dateString = String.valueOf(time);
            DecimalFormat decimalFormat = new DecimalFormat("0000000000");
            this.deploymentId = dateString.length() > 9 ? dateString.substring(dateString.length() - 10) :
                    decimalFormat.format(time);
        }
    }


}
