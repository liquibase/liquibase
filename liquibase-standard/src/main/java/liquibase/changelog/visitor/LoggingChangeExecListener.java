package liquibase.changelog.visitor;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer;

public class LoggingChangeExecListener extends AbstractChangeExecListener {
    @Override
    public void willRun(ChangeSet changeSet,
                        DatabaseChangeLog databaseChangeLog, Database database,
                        ChangeSet.RunStatus runStatus) {
        Scope.getCurrentScope().getLog(getClass()).info("EVENT: willRun fired");
    }

    @Override
    public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog,
                    Database database, ChangeSet.ExecType execType) {
        Scope.getCurrentScope().getLog(getClass()).info("EVENT: ran fired");
    }

    /**
     *
     * Called before a change is rolled back.
     *
     * @param changeSet         changeSet that was rolled back
     * @param databaseChangeLog parent change log
     * @param database          the database the rollback was executed on.
     *
     */
    @Override
    public void willRollback(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        Scope.getCurrentScope().getLog(getClass()).info("EVENT: willRollback fired");
    }

    /**
     *
     * Called when there is a rollback failure
     *
     * @param changeSet         changeSet that was rolled back
     * @param databaseChangeLog parent change log
     * @param database          the database the rollback was executed on.
     * @param exception         the original exception which was thrown
     *
     */
    @Override
    public void rollbackFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception e) {
        Scope.getCurrentScope().getLog(getClass()).info("EVENT: rollbackFailed fired");
    }

    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        Scope.getCurrentScope().getLog(getClass()).info("rolledBack");
    }

    @Override
    public void preconditionFailed(PreconditionFailedException error,
                                   PreconditionContainer.FailOption onFail) {
        Scope.getCurrentScope().getLog(getClass()).info("EVENT: preconditionFailed");
    }

    @Override
    public void preconditionErrored(PreconditionErrorException error,
                                    PreconditionContainer.ErrorOption onError) {
        Scope.getCurrentScope().getLog(getClass()).info("EVENT: preconditionErrored");
    }
}
