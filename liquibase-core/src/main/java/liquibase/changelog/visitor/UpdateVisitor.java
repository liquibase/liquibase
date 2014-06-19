package liquibase.changelog.visitor;

import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

import java.util.Set;

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
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, RuntimeEnvironment runtimeEnvironment, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        ChangeSet.RunStatus runStatus = this.database.getRunStatus(changeSet);
        log.debug("Running Changeset:" + changeSet);
        fireWillRun(changeSet, databaseChangeLog, runtimeEnvironment, runStatus);
        ChangeSet.ExecType execType = changeSet.execute(databaseChangeLog, execListener, this.database);
        if (!runStatus.equals(ChangeSet.RunStatus.NOT_RAN)) {
            execType = ChangeSet.ExecType.RERAN;
        }
        fireRan(changeSet, databaseChangeLog, runtimeEnvironment, execType);
        // reset object quoting strategy after running changeset
        this.database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        this.database.markChangeSetExecStatus(changeSet, execType);

        this.database.commit();
    }

    private void fireWillRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, RuntimeEnvironment runtimeEnvironment, RunStatus runStatus) {
      if (execListener != null) {
        execListener.willRun(changeSet, databaseChangeLog, database, runStatus);
      }      
    }

    private void fireRan(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, RuntimeEnvironment runtimeEnvironment, ExecType execType) {
      if (execListener != null) {
        execListener.ran(changeSet, databaseChangeLog, database, execType);
      }
    }
}
