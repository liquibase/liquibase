package liquibase.hub.listener;

import liquibase.Liquibase;
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
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.*;
import liquibase.logging.Logger;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class HubChangeExecListener implements ChangeExecListener {
    private static final Logger logger = Scope.getCurrentScope().getLog(HubChangeExecListener.class);

    private final Operation operation;

    private Map<ChangeSet, Date> startDateMap = new HashMap<>();

    public HubChangeExecListener(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        System.out.println("Hub will run");
        startDateMap.put(changeSet, new Date());
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        System.out.println("Hub change will run");

    }

    @Override
    public void ran(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.ExecType execType) {
        HubChangeLog hubChangeLog;
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        try {
            hubChangeLog = hubService.getChangeLog(UUID.fromString(databaseChangeLog.getChangeLogId()));
            if (hubChangeLog == null) {
                logger.warning("The changelog '" + databaseChangeLog.getPhysicalFilePath() + "' has not been registered with Hub");
                return;
            }
        }
        catch (LiquibaseHubException lhe) {
            logger.warning("The changelog '" + databaseChangeLog.getPhysicalFilePath() + "' has not been registered with Hub");
            return;
        }

        //
        //  POST /organizations/{id}/projects/{id}/operations/{id}/change-events
        //
        List<Change> changes = changeSet.getChanges();
        List<String> sqlList = new ArrayList<>();
        for (Change change : changes) {

            //
            // Until the generated SQL field is changed to be a CLOB
            // we are truncating the SQL to fit
            //
            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(change, database);
            for (Sql sql : sqls) {
                sqlList.add(sql.toSql());
            }
        }

        String[] sqlArray = new String[sqlList.size()];
        sqlArray = sqlList.toArray(sqlArray);
        OperationChangeEvent operationChangeEvent = new OperationChangeEvent();
        operationChangeEvent.setEventType("UPDATE");
        operationChangeEvent.setStartDate(startDateMap.get(changeSet));
        operationChangeEvent.setEndDate(new Date());
        operationChangeEvent.setChangesetId(changeSet.getId());
        operationChangeEvent.setChangesetFilename(changeSet.getFilePath());
        operationChangeEvent.setChangesetAuthor(changeSet.getAuthor());
        operationChangeEvent.setOperationStatusType("PASS");
        operationChangeEvent.setGeneratedSql(sqlArray);
        operationChangeEvent.setOperation(operation);
        operationChangeEvent.setLogsTimestamp(new Date());
        operationChangeEvent.setLogs("LOGS");
        operationChangeEvent.setStatusMessage("STATUS MESSAGE");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChangeLogSerializer serializer = ChangeLogSerializerFactory.getInstance().getSerializer(".json");
        try {
            serializer.write(Collections.singletonList(changeSet), baos);
            operationChangeEvent.setChangesetBody(baos.toString("UTF-8"));
        }
        catch (IOException ioe) {
            //
            // Consume
            //
        }
        operationChangeEvent.setProject(hubChangeLog.getPrj());
        operationChangeEvent.setOperation(operation);
        try {
            hubService.sendOperationChangeEvent(operationChangeEvent);
        }
        catch (LiquibaseException lbe) {
            logger.warning("Unable to send Operation Change Event for operation '" + operation.getId().toString() +
                    " change set '" + changeSet.toString(false));
        }
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
        System.out.println("Hub change ran");        //

    }

    @Override
    public void runFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        System.out.println("Hub run failed");
    }
}
