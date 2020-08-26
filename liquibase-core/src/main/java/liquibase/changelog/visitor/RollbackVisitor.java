package liquibase.changelog.visitor;

import liquibase.change.Change;
import liquibase.change.core.SQLFileChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RollbackContainer;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.logging.LogService;
import liquibase.logging.LogType;

import java.util.List;
import java.util.Set;

public class RollbackVisitor implements ChangeSetVisitor {

    private Database database;

    private ChangeExecListener execListener;

    /**
     * @deprecated - please use the constructor with ChangeExecListener, which can be null.
     */
    @Deprecated
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
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        if (! (executor instanceof LoggingExecutor)) {
            LogService.getLog(getClass()).info(LogType.USER_MESSAGE, "Rolling Back Changeset:" + changeSet);
        }
        changeSet.rollback(this.database, this.execListener);
        this.database.removeRanStatus(changeSet);
        sendRollbackEvent(changeSet, databaseChangeLog, database);
        this.database.commit();
        checkForEmptyRollbackFile(changeSet);
    }

    private void checkForEmptyRollbackFile(ChangeSet changeSet) {
        RollbackContainer container = changeSet.getRollback();
        List<Change> changes = container.getChanges();
        if (changes.isEmpty()) {
            return;
        }
        for (Change change : changes) {
            if (! (change instanceof SQLFileChange)) {
                continue;
            }
            String sql = ((SQLFileChange)change).getSql();
            if (sql.length() == 0) {
                LogService.getLog(getClass())
                          .info("\nNo rollback logic defined in empty rollback script. Changesets have been removed from\n" +
                                "the DATABASECHANGELOG table but no other logic was performed.");
            }
        }
    }

    private void sendRollbackEvent(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database2) {
        if (execListener != null) {
            execListener.rolledBack(changeSet, databaseChangeLog, database);
        }
    }
}
