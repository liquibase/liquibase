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
    
    public void visit(ChangeSet changeSet, Database database) throws LiquibaseException {
        log.finer("Running Changeset:" + changeSet);
        if (changeSet.execute(this.database)) {
            if (this.database.getRunStatus(changeSet).equals(ChangeSet.RunStatus.NOT_RAN)) {
                this.database.markChangeSetAsRan(changeSet);
            } else {
                this.database.markChangeSetAsReRan(changeSet);
            }
        }

        this.database.commit();
    }
}
