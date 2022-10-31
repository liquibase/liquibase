package liquibase.changelog.visitor;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultChangeExecListener implements ChangeExecListener {
    List<ChangeExecListener> listeners;
    List<ChangeSet> deployedChangeSets = new ArrayList<>();

    public DefaultChangeExecListener(ChangeExecListener... listeners) {
        this.listeners = Arrays.stream(listeners).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Called just before a given changeset is run.
     *
     * @param changeSet         that will be run
     * @param databaseChangeLog parent changelog
     * @param database          the database the change will be run against
     * @param runStatus         of the current change from the database
     */
    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        listeners.forEach(listener -> listener.willRun(changeSet, databaseChangeLog, database, runStatus));
    }

    /**
     * Called after the given changeset is run.
     *
     * @param changeSet         changeSet that was run
     * @param databaseChangeLog the parent changelog
     * @param database          the database the change was run against
     * @param execType          is the result
     */
    @Override
    public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.ExecType execType) {
        deployedChangeSets.add(changeSet);
        listeners.forEach(listener -> listener.ran(changeSet, databaseChangeLog, database, execType));
    }

    /**
     * Called before a change is rolled back.
     *
     * @param changeSet         changeSet that was rolled back
     * @param databaseChangeLog parent change log
     * @param database          the database the rollback was executed on.
     */
    @Override
    public void willRollback(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        listeners.forEach(listener -> listener.willRollback(changeSet, databaseChangeLog, database));
    }

    /**
     * Called after a change is rolled back.
     *
     * @param changeSet         changeSet that was rolled back
     * @param databaseChangeLog parent change log
     * @param database          the database the rollback was executed on.
     */
    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        listeners.forEach(listener -> listener.rolledBack(changeSet, databaseChangeLog, database));
    }

    /**
     * @param error
     * @param onFail
     */
    @Override
    public void preconditionFailed(PreconditionFailedException error, PreconditionContainer.FailOption onFail) {
        listeners.forEach(listener -> listener.preconditionFailed(error, onFail));
    }

    /**
     * @param error
     * @param onError
     */
    @Override
    public void preconditionErrored(PreconditionErrorException error, PreconditionContainer.ErrorOption onError) {
        listeners.forEach(listener -> listener.preconditionErrored(error, onError));
    }

    /**
     * @param change
     * @param changeSet
     * @param changeLog
     * @param database
     */
    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        listeners.stream().filter(Objects::nonNull).forEach(listener -> listener.willRun(change, changeSet, changeLog, database));
    }

    /**
     * @param change
     * @param changeSet
     * @param changeLog
     * @param database
     */
    @Override
    public void ran(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        listeners.forEach(listener -> listener.ran(change, changeSet, changeLog, database));
    }

    /**
     * @param changeSet
     * @param databaseChangeLog
     * @param database
     * @param exception
     */
    @Override
    public void runFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        listeners.forEach(listener -> listener.runFailed(changeSet, databaseChangeLog, database, exception));
    }

    /**
     * @param changeSet
     * @param databaseChangeLog
     * @param database
     * @param exception
     */
    @Override
    public void rollbackFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        listeners.forEach(listener -> listener.rollbackFailed(changeSet, databaseChangeLog, database, exception));
    }

    public List<ChangeSet> getDeployedChangeSets() {
        return deployedChangeSets;
    }
}
