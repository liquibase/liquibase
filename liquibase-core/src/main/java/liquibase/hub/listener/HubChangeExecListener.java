package liquibase.hub.listener;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.Operation;
import liquibase.hub.model.OperationEvent;
import liquibase.precondition.core.PreconditionContainer;

public class HubChangeExecListener implements ChangeExecListener {

    private final Operation operation;

    public HubChangeExecListener(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        System.out.println("Hub will run");
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        System.out.println("Hub change will run");

    }

    @Override
    public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.ExecType execType) {
        System.out.println("Hub ran run");

    }

    @Override
    public void rolledBack(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        System.out.println("Hub rollback run");

    }

    @Override
    public void preconditionFailed(PreconditionFailedException error, PreconditionContainer.FailOption onFail) {
        System.out.println("Hub precondition failed");
    }

    @Override
    public void preconditionErrored(PreconditionErrorException error, PreconditionContainer.ErrorOption onError) {
        System.out.println("Hub precondition errored");
    }

    @Override
    public void ran(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        //
        //  POST /organizations/{id}/projects/{id}/operations/{id}/event
        //
        System.out.println("Hub change ran");
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        OperationEvent operationEvent =
           new OperationEvent("changeImpact", operation, null, null, 0, null, null);
        try {
            hubService.sendOperationEvent(operationEvent);
        }
        catch (LiquibaseException lbe) {

        }
    }

    @Override
    public void runFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        System.out.println("Hub run failed");
    }
}
