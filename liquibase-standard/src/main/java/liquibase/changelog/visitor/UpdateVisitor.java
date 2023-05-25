package liquibase.changelog.visitor;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.logging.mdc.MdcKey;

import java.util.Objects;
import java.util.Set;

public class UpdateVisitor implements ChangeSetVisitor {

    private final Database database;

    private ChangeExecListener execListener;

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

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database,
                      Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        logMdcData(changeSet);
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (! (executor instanceof LoggingExecutor)) {
            Scope.getCurrentScope().getUI().sendMessage("Running Changeset: " + changeSet);
        }
        ChangeSet.RunStatus runStatus = this.database.getRunStatus(changeSet);
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
        if (!Objects.equals(runStatus, ChangeSet.RunStatus.NOT_RAN) && Objects.equals(execType, ExecType.EXECUTED)) {
            execType = ChangeSet.ExecType.RERAN;
        }
        fireRan(changeSet, databaseChangeLog, database, execType);
        addAttributesForMdc(changeSet, execType);
        // reset object quoting strategy after running changeset
        this.database.setObjectQuotingStrategy(previousStr);
        this.database.markChangeSetExecStatus(changeSet, execType);

        this.database.commit();
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
        ChangeLogHistoryService changelogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        String deploymentId = changelogService.getDeploymentId();
        changeSet.setAttribute("deploymentId", deploymentId);
    }
}
