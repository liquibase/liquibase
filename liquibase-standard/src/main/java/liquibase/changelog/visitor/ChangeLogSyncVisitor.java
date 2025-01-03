package liquibase.changelog.visitor;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.util.ISODateFormat;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ChangeLogSyncVisitor implements ChangeSetVisitor {

    private Database database;
    private ChangeLogSyncListener listener;

    public ChangeLogSyncVisitor(Database database) {
        this.database = database;
    }

    public ChangeLogSyncVisitor(Database database, ChangeLogSyncListener listener) {
        this.database = database;
        this.listener = listener;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        try {
            preRunMdc(changeSet);
            this.database.markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
            postRunMdc(changeSet);
            if(listener != null) {
                listener.markedRan(changeSet, databaseChangeLog, database);
            }
        } catch (Exception e) {
            Date stop = new Date();
            changeSet.setOperationStopTime(stop);
            try (MdcObject stopTime = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_STOP_TIME, new ISODateFormat().format(stop));
                 MdcObject changelogSyncOutcome = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_SYNC_OUTCOME, MdcValue.COMMAND_FAILED)) {
                Scope.getCurrentScope().getLog(getClass()).fine("Failed syncing changeset");
            }
            if (listener != null) {
                listener.markedRanFailed(changeSet, databaseChangeLog, database, e);
            }
        }

    }

    private void preRunMdc(ChangeSet changeSet) {
        Date start = new Date();
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_START_TIME, new ISODateFormat().format(start));
        changeSet.setOperationStartTime(start);
        logMdcData(changeSet);
        changeSet.addChangeSetMdcProperties();
    }

    private void postRunMdc(ChangeSet changeSet) {
        try {
            ChangeLogHistoryServiceFactory instance = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class);
            String deploymentId = Scope.getCurrentScope().getDeploymentId();
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_ID, deploymentId);
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Failed to retrieve deployment ID for MDC", e);
        }

        AtomicInteger changesetCount = Scope.getCurrentScope().get("changesetCount", AtomicInteger.class);
        if (changesetCount != null) {
            changesetCount.getAndIncrement();
        }
        Date stop = new Date();
        changeSet.setOperationStopTime(stop);
        try (MdcObject stopTime = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_STOP_TIME, new ISODateFormat().format(stop));
             MdcObject changelogSyncOutcome = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_SYNC_OUTCOME, MdcValue.COMMAND_SUCCESSFUL)) {
            Scope.getCurrentScope().getLog(getClass()).fine("Finished syncing changeset");
        }
    }
}
