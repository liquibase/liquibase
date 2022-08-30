package liquibase.changelog.visitor;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer.ErrorOption;
import liquibase.precondition.core.PreconditionContainer.FailOption;

/*
 * Default implementation of the ChangeExecListener so that sub classes can just override the methods
 * they are interested in.
 */
public abstract class AbstractChangeExecListener implements ChangeExecListener {
    @Override
    public void willRun(ChangeSet changeSet,
            DatabaseChangeLog databaseChangeLog, Database database,
            RunStatus runStatus) {
    }

    @Override
    public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog,
            Database database, ExecType execType) {
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
    }

    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
    }

    @Override
    public void preconditionFailed(PreconditionFailedException error,
            FailOption onFail) {
    }

    @Override
    public void preconditionErrored(PreconditionErrorException error,
            ErrorOption onError) {
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet,
            DatabaseChangeLog changeLog, Database database) {
    }

    @Override
    public void ran(Change change, ChangeSet changeSet,
            DatabaseChangeLog changeLog, Database database) {
    }

    @Override
    public void runFailed(ChangeSet changeSet,
            DatabaseChangeLog databaseChangeLog, Database database,
            Exception exception) {
    }
}
