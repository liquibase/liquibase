package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;

public class RollbackVisitor implements ChangeSetVisitor {

    private Database database;

    public RollbackVisitor(Database database) {
        this.database = database;
    }

    public Direction getDirection() {
        return ChangeSetVisitor.Direction.REVERSE;
    }

    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) throws LiquibaseException {
        LogFactory.getLogger().info("Rolling Back Changeset:" + changeSet);
        changeSet.rollback(this.database);
        this.database.removeRanStatus(changeSet);

        this.database.commit();

    }
}
