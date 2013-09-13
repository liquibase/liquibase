package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

public class UpdateVisitor implements ChangeSetVisitor {

    private Database database;

    private Logger log = LogFactory.getLogger();
    
    private ChangeExecListener execListener;

    public UpdateVisitor(Database database) {
        this.database = database;
    }
    
    public UpdateVisitor(Database database, ChangeExecListener execListener) {
      this(database);
      this.execListener = execListener;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) throws LiquibaseException {
        ChangeSet.RunStatus runStatus = this.database.getRunStatus(changeSet);
        log.debug("Running Changeset:" + changeSet);
        sendWillRunEvent(changeSet, databaseChangeLog, database, runStatus);
        ChangeSet.ExecType execType = changeSet.execute(databaseChangeLog, this.database);
        if (!runStatus.equals(ChangeSet.RunStatus.NOT_RAN)) {
            execType = ChangeSet.ExecType.RERAN;
        }
        sendRanEvent(changeSet, databaseChangeLog, database, execType);
        // reset object quoting strategy after running changeset
        this.database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        this.database.markChangeSetExecStatus(changeSet, execType);

        this.database.commit();
    }

    private void sendWillRunEvent(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database2, RunStatus runStatus) {
      if (execListener != null) {
        execListener.willRun(changeSet, databaseChangeLog, database, runStatus);
      }      
    }

    private void sendRanEvent(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database2, ExecType execType) {
      if (execListener != null) {
        execListener.ran(changeSet, databaseChangeLog, database, execType);
      }
    }
}
