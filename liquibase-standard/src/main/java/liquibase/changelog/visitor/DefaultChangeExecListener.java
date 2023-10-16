package liquibase.changelog.visitor;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A wrapper ChangeExecListener who keeps track of deployed and failed ChangeSets,
 * while also delegating listener actions to any other ChangeExecListener included
 * when the object is constructed.
 */
public class DefaultChangeExecListener implements ChangeExecListener, ChangeLogSyncListener {
    private final List<ChangeExecListener> listeners;
    private final List<ChangeSet> deployedChangeSets = new LinkedList<>();
    @Getter
    private final List<ChangeSet> rolledBackChangeSets = new LinkedList<>();
    private final List<ChangeSet> failedChangeSets = new LinkedList<>();
    @Getter
    private final List<ChangeSet> failedRollbackChangeSets = new LinkedList<>();
    private final Map<ChangeSet, List<Change>> deployedChangesPerChangeSet = new ConcurrentHashMap<>();

    public DefaultChangeExecListener(ChangeExecListener... listeners) {
        this.listeners = Arrays.stream(listeners)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        listeners.forEach(listener -> listener.willRun(changeSet, databaseChangeLog, database, runStatus));
    }

    @Override
    public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.ExecType execType) {
        deployedChangeSets.add(changeSet);
        listeners.forEach(listener -> listener.ran(changeSet, databaseChangeLog, database, execType));
    }

    @Override
    public void willRollback(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        listeners.forEach(listener -> listener.willRollback(changeSet, databaseChangeLog, database));
    }

    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        rolledBackChangeSets.add(changeSet);
        listeners.forEach(listener -> listener.rolledBack(changeSet, databaseChangeLog, database));
    }

    @Override
    public void preconditionFailed(PreconditionFailedException error, PreconditionContainer.FailOption onFail) {
        listeners.forEach(listener -> listener.preconditionFailed(error, onFail));
    }

    @Override
    public void preconditionErrored(PreconditionErrorException error, PreconditionContainer.ErrorOption onError) {
        listeners.forEach(listener -> listener.preconditionErrored(error, onError));
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        listeners.forEach(listener -> listener.willRun(change, changeSet, changeLog, database));
    }

    @Override
    public void ran(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        deployedChangesPerChangeSet.computeIfAbsent(changeSet, val -> new LinkedList<>()).add(change);
        listeners.forEach(listener -> listener.ran(change, changeSet, changeLog, database));
    }

    @Override
    public void runFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        failedChangeSets.add(changeSet);
        listeners.forEach(listener -> listener.runFailed(changeSet, databaseChangeLog, database, exception));
    }

    @Override
    public void rollbackFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        failedRollbackChangeSets.add(changeSet);
        listeners.forEach(listener -> listener.rollbackFailed(changeSet, databaseChangeLog, database, exception));
    }

    /**
     * Get the list of ChangeSets that have been deployed during a given Liquibase command.
     * For example: if you ran update with three ChangeSets in total and the third ChangeSet failed,
     * this list will contain the first two ChangeSets that were executed without exception.
     *
     * @return the list of ChangeSets deployed during a command.
     */
    public List<ChangeSet> getDeployedChangeSets() {
        return deployedChangeSets;
    }

    /**
     * Gets list of failed ChangeSets encountered during a given Liquibase command.
     *
     * @return the list of ChangeSets which have failed to deploy.
     */
    public List<ChangeSet> getFailedChangeSets() {
        return failedChangeSets;
    }

    /**
     * Gets list of Changes deployed during the current ChangeSet execution. This list is dynamic and will update depending on where in the lifecycle this is being called.
     *
     * @param changeSet the ChangeSet to find deployed changes from.
     * @return the list of Changes which have deployed from the given ChangeSet. List will be empty if changes have not deployed from requested ChangeSet.
     */
    public List<Change> getDeployedChanges(ChangeSet changeSet) {
        List<Change> changesDeployed = deployedChangesPerChangeSet.get(changeSet);
        return changesDeployed != null ? changesDeployed : new LinkedList<>();
    }

    public void addListener(ChangeExecListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * @param changeSet
     * @param databaseChangeLog
     * @param database
     */
    @Override
    public void markedRan(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        // no-op
    }

    public void reset() {
        this.deployedChangeSets.clear();
        this.failedChangeSets.clear();
        this.deployedChangesPerChangeSet.clear();
    }
}
