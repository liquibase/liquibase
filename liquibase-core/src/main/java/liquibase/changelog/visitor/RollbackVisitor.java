package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;

public class RollbackVisitor implements ChangeSetVisitor {

    private Database database;
    
    private ChangeExecListener execListener;

    public RollbackVisitor(Database database) {
        this.database = database;
    }

    public RollbackVisitor(Database database, ChangeExecListener listener) {
      this(database);
      this.execListener = listener;
  }
    
    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.REVERSE;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) throws LiquibaseException {
        LogFactory.getInstance().getLog().info("Rolling Back Changeset:" + changeSet);
        changeSet.rollback(this.database);
        this.database.removeRanStatus(changeSet);
        sendRollbackEvent(changeSet, databaseChangeLog, database);
        this.database.commit();

    }

    private void sendRollbackEvent(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database2) {
      if (execListener != null) {
        execListener.rolledBack(changeSet, databaseChangeLog, database);
      }
    }
}
