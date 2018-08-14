package liquibase.changelog.visitor;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer;

/**
 * An implementation of ChangeExecListener can be called by UpdateVisitor or
 * RollbackVisitor for each changeset that is actually run.
 *
 * @author suehs
 */
public interface ChangeExecListener {

    /**
     * Called just before a given changeset is run.
     *
     * @param changeSet         that will be run
     * @param databaseChangeLog parent changelog
     * @param database          the database the change will be run against
     * @param runStatus         of the current change from the database
     */
    void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, RunStatus runStatus);

    /**
     * Called after the given changeset is run.
     *
     * @param changeSet         changeSet that was run
     * @param databaseChangeLog the parent changelog
     * @param database          the database the change was run against
     * @param execType          is the result
     */
    void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ExecType execType);

    /**
     * Called after a change is rolled back.
     *
     * @param changeSet         changeSet that was rolled back
     * @param databaseChangeLog parent change log
     * @param database          the database the rollback was executed on.
     */
    void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database);

    void preconditionFailed(PreconditionFailedException error, PreconditionContainer.FailOption onFail);

    void preconditionErrored(PreconditionErrorException error, PreconditionContainer.ErrorOption onError);

    void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database);

    void ran(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database);

    void runFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception);
}
