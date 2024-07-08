package liquibase.changelog.visitor;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;

import java.util.Objects;
import java.util.Set;

public class UpdateVisitor implements ChangeSetVisitor {

    private final Database database;

    private ChangeExecListener execListener;

    private ShouldRunChangeSetFilter shouldRunChangeSetFilter;

    /**
     * @deprecated - please use the constructor with ChangeExecListener, which can be null.
     */
    @Deprecated
    public UpdateVisitor(Database database) {
        this.database = database;
    }

    public UpdateVisitor(Database database, ChangeExecListener execListener) {
        this(database);
        this.execListener = execListener;
    }

    public UpdateVisitor(Database database, ChangeExecListener execListener, ShouldRunChangeSetFilter shouldRunChangeSetFilter) {
        this(database);
        this.execListener = execListener;
        this.shouldRunChangeSetFilter = shouldRunChangeSetFilter;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database,
                      Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        logMdcData(changeSet);

        // if we don't have shouldRunChangeSetFilter go on with the old behavior assuming that it has been validated before
        boolean isAccepted = this.shouldRunChangeSetFilter == null || this.shouldRunChangeSetFilter.accepts(changeSet).isAccepted();
        CheckSum oldChecksum = updateCheckSumIfRequired(changeSet);
        if (isAccepted) {
            executeAcceptedChange(changeSet, databaseChangeLog, database);
            this.database.commit();
        } else if ((oldChecksum == null || oldChecksum.getVersion() < ChecksumVersion.latest().getVersion())) {
            upgradeCheckSumVersionForAlreadyExecutedOrNullChange(changeSet, database, oldChecksum);
            this.database.commit();
        }
    }

    /**
     * Updates the checksum to the current version in Changeset object in case that it is null
     * or if it is from a previous checksum version.
     *
     * @return oldChecksum the former checksum
     */
    private static CheckSum updateCheckSumIfRequired(ChangeSet changeSet) {
        CheckSum oldChecksum = changeSet.getStoredCheckSum();
        if (oldChecksum == null || oldChecksum.getVersion() < ChecksumVersion.latest().getVersion()) {
            changeSet.clearCheckSum();
            changeSet.setStoredCheckSum(changeSet.generateCheckSum(ChecksumVersion.latest()));
        }
        return oldChecksum;
    }

    /**
     * Upgrade checksum for a given Changeset at database.
     */
    private static void upgradeCheckSumVersionForAlreadyExecutedOrNullChange(ChangeSet changeSet, Database database, CheckSum oldChecksum) throws DatabaseException {
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (!(executor instanceof LoggingExecutor) && oldChecksum != null) {
            Scope.getCurrentScope().getUI().sendMessage(String.format("Upgrading checksum for Changeset %s from %s to %s.",
                    changeSet, oldChecksum, changeSet.getStoredCheckSum()));
        }
        ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        changeLogService.replaceChecksum(changeSet);
    }

    /**
     * Executes the given changeset marking it as executed/reran/etc at the database
     */
    private void executeAcceptedChange(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database)
            throws DatabaseException, DatabaseHistoryException, MigrationFailedException {
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (!(executor instanceof LoggingExecutor)) {
            Scope.getCurrentScope().getUI().sendMessage("Running Changeset: " + changeSet);
        }
        RunStatus runStatus = this.database.getRunStatus(changeSet);
        Scope.getCurrentScope().getLog(getClass()).fine("Running Changeset: " + changeSet);
        fireWillRun(changeSet, databaseChangeLog, database, runStatus);
        ExecType execType;
        ObjectQuotingStrategy previousStr = this.database.getObjectQuotingStrategy();
        try {
            execType = changeSet.execute(databaseChangeLog, execListener, this.database);

        } catch (MigrationFailedException e) {
            fireRunFailed(changeSet, databaseChangeLog, database, e);
            throw e;
        }
        if (!Objects.equals(runStatus, RunStatus.NOT_RAN) && Objects.equals(execType, ExecType.EXECUTED)) {
            execType = ExecType.RERAN;
        }
        addAttributesForMdc(changeSet, execType);
        // reset object quoting strategy after running changeset
        this.database.setObjectQuotingStrategy(previousStr);
        this.database.markChangeSetExecStatus(changeSet, execType);
        fireRan(changeSet, databaseChangeLog, database, execType);
    }

    protected void fireRunFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, MigrationFailedException e) {
        if (execListener != null) {
            execListener.runFailed(changeSet, databaseChangeLog, database, e);
        }
    }

    protected void fireWillRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database2, RunStatus runStatus) {
      if (execListener != null) {
        execListener.willRun(changeSet, databaseChangeLog, database, runStatus);
      }      
    }

    protected void fireRan(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database2, ExecType execType) {
      if (execListener != null) {
        execListener.ran(changeSet, databaseChangeLog, database, execType);
      }
    }

    private void addAttributesForMdc(ChangeSet changeSet, ExecType execType) {
        changeSet.setAttribute("updateExecType", execType);
        ChangeLogHistoryService changelogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        String deploymentId = changelogService.getDeploymentId();
        changeSet.setAttribute("deploymentId", deploymentId);
    }
}

