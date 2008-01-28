package liquibase.parser.visitor;

import liquibase.ChangeSet;
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

    public void visit(ChangeSet changeSet) throws LiquibaseException {
        database.markChangeSetAsRan(changeSet);
    }
}
