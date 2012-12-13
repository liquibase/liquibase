package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

public class RollbackVisitor implements ChangeSetVisitor {

    private Database database;

    public RollbackVisitor(Database database) {
        this.database = database;
    }

    public Direction getDirection() {
        return ChangeSetVisitor.Direction.REVERSE;
    }

    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) throws LiquibaseException {
        Logger log = LogFactory.getLogger();
        try {
            log.pushContext(changeSet.toString(false));
            log.info("Rolling Back Changeset:" + changeSet);
            changeSet.rollback(this.database);
            this.database.removeRanStatus(changeSet);

            this.database.commit();
        } finally {
            log.popContext();
        }
    }
}
