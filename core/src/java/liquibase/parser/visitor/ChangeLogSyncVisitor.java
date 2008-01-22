package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.log.LogFactory;

import java.util.logging.Logger;

public class ChangeLogSyncVisitor implements ChangeSetVisitor {

    private Database database;

    private Logger log = LogFactory.getLogger();

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
