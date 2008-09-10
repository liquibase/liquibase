package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.log.LogFactory;

public class RollbackVisitor implements ChangeSetVisitor {

    private Database database;

    public RollbackVisitor(Database database) {
        this.database = database;
    }

    public Direction getDirection() {
        return ChangeSetVisitor.Direction.REVERSE;
    }

    public void visit(ChangeSet changeSet, Database database) throws LiquibaseException {
        LogFactory.getLogger().info("Rolling Back Changeset:" + changeSet);
        changeSet.rolback(this.database);
        this.database.removeRanStatus(changeSet);

        this.database.commit();

    }
}
