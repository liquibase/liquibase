package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

public class ChangeLogSyncVisitor implements ChangeSetVisitor {

    private Database database;

    public ChangeLogSyncVisitor(Database database) {
        this.database = database;
    }

    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) throws LiquibaseException {
        this.database.markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
    }
}
