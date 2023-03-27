package liquibase.changelog.visitor;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
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
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_START_TIME, new ISODateFormat().format(new Date()));
        logMdcData(changeSet);
        changeSet.addChangeSetMdcProperties();
        this.database.markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
        if(listener != null) {
            listener.markedRan(changeSet, databaseChangeLog, database);
        }
        AtomicInteger changesetCount = Scope.getCurrentScope().get("changesetCount", AtomicInteger.class);
        if (changesetCount != null) {
            changesetCount.getAndIncrement();
        }
        try (MdcObject stopTime = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_STOP_TIME, new ISODateFormat().format(new Date()))) {
            Scope.getCurrentScope().getLog(getClass()).info("Finished syncing changeset");
        }
    }
}
