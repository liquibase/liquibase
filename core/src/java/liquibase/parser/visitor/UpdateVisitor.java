package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.log.LogFactory;

import java.util.logging.Logger;

public class UpdateVisitor implements ChangeSetVisitor {

    private Database database;

    private Logger log = LogFactory.getLogger();

    public UpdateVisitor(Database database) {
        this.database = database;
    }

    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }
    
    public void visit(ChangeSet changeSet) throws LiquibaseException {
        log.info("Running Changeset:" + changeSet);
        changeSet.execute(database);
        if (database.getRunStatus(changeSet).equals(ChangeSet.RunStatus.NOT_RAN)) {
            database.markChangeSetAsRan(changeSet);
        } else {
            database.markChangeSetAsReRan(changeSet);
        }

        database.commit();
    }
}
