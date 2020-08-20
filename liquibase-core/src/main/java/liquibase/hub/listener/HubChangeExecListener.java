package liquibase.hub.listener;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Operation;
import liquibase.hub.model.OperationChangeEvent;
import liquibase.logging.Logger;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class HubChangeExecListener extends AbstractChangeExecListener
                                   implements ChangeExecListener {
    private static final Logger logger = Scope.getCurrentScope().getLog(HubChangeExecListener.class);

    private final Operation operation;

    private final Map<ChangeSet, Date> startDateMap = new HashMap<>();

    public HubChangeExecListener(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        startDateMap.put(changeSet, new Date());
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        startDateMap.put(changeSet, new Date());
    }

    @Override
    public void ran(ChangeSet changeSet,
                    DatabaseChangeLog databaseChangeLog,
                    Database database,
                    ChangeSet.ExecType execType) {
        String message = "PASSED::" + changeSet.getId() + "::" + changeSet.getAuthor();
        updateHub(changeSet, databaseChangeLog, database, "PASS", message);
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
        startDateMap.put(changeSet, new Date());
    }

    /**
     *
     * Called when there is a rollback failure
     *
     * @param changeSet         changeSet that was rolled back
     * @param databaseChangeLog parent change log
     * @param database          the database the rollback was executed on.
     * @param e                 original exception
     *
     */
    @Override
    public void rollbackFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception e) {
        updateHubForRollback(changeSet, databaseChangeLog, database, "FAIL", e.getMessage());
    }

    /**
     *
     * Called which a change set is successfully rolled back
     *
     * @param changeSet         changeSet that was rolled back
     * @param databaseChangeLog parent change log
     * @param database          the database the rollback was executed on.
     *
     */
    @Override
    public void rolledBack(ChangeSet changeSet,
                           DatabaseChangeLog databaseChangeLog,
                           Database database) {
        String message = "PASSED::" + changeSet.getId() + "::" + changeSet.getAuthor();
        updateHubForRollback(changeSet, databaseChangeLog, database, "PASS", message);
    }

    @Override
    public void preconditionFailed(PreconditionFailedException error, PreconditionContainer.FailOption onFail) {
    }

    @Override
    public void preconditionErrored(PreconditionErrorException error, PreconditionContainer.ErrorOption onError) {
    }

    @Override
    public void ran(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {

    }

    @Override
    public void runFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        updateHub(changeSet, databaseChangeLog, database, "FAIL", exception.getMessage());
    }

    //
    // Send an update message to Hub for this change set rollback
    //
    private void updateHubForRollback(ChangeSet changeSet,
                                      DatabaseChangeLog databaseChangeLog,
                                      Database database,
                                      String operationStatusType,
                                      String statusMessage) {
        if (operation == null) {
            boolean realTime = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("realtime");
            if (realTime) {
                String message =
                        "Hub communication failure.\n" +
                        "The data for operation on changeset '" +
                        changeSet.getId() +
                        "' by author '" + changeSet.getAuthor() + "'\n" +
                        "was not successfully recorded in your Liquibase Hub project";
                Scope.getCurrentScope().getUI().sendMessage(message);
                logger.info(message);
            }
            return;
        }
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
        OperationChangeEvent operationChangeEvent = new OperationChangeEvent();
        operationChangeEvent.setEventType("ROLLBACK");
        operationChangeEvent.setStartDate(startDateMap.get(changeSet));
        operationChangeEvent.setEndDate(new Date());
        operationChangeEvent.setChangesetId(changeSet.getId());
        operationChangeEvent.setChangesetFilename(changeSet.getFilePath());
        operationChangeEvent.setChangesetAuthor(changeSet.getAuthor());
        List<String> sqlList = new ArrayList<>();
        try {
           if (changeSet.hasCustomRollbackChanges()) {
               List<Change> changes = changeSet.getRollback().getChanges();
               for (Change change : changes) {
                    SqlStatement[] statements = change.generateStatements(database);
                    for (SqlStatement statement : statements) {
                        for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, database)) {
                            sqlList.add(sql.toSql());
                        }
                    }
                }
           }
           else {
               List<Change> changes = changeSet.getChanges();
               for (Change change : changes) {
                   SqlStatement[] statements = change.generateRollbackStatements(database);
                   for (SqlStatement statement : statements) {
                        for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, database)) {
                            sqlList.add(sql.toSql());
                        }
                    }
                }
            }
        }
        catch (LiquibaseException lbe) {
            logger.warning(lbe.getMessage());
        }

        String[] sqlArray = new String[sqlList.size()];
        sqlArray = sqlList.toArray(sqlArray);
        operationChangeEvent.setGeneratedSql(sqlArray);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChangeLogSerializer serializer =
                ChangeLogSerializerFactory.getInstance().getSerializer(".json");
        try {
            serializer.write(Collections.singletonList(changeSet), baos);
            operationChangeEvent.setChangesetBody(baos.toString("UTF-8"));
        }
        catch (IOException ioe) {
            //
            // Consume
            //
        }
        operationChangeEvent.setOperationStatusType(operationStatusType);
        operationChangeEvent.setStatusMessage(statusMessage);
        operationChangeEvent.setLogs("LOGS");
        operationChangeEvent.setLogsTimestamp(new Date());

        operationChangeEvent.setProject(hubChangeLog.getPrj());
        operationChangeEvent.setOperation(operation);

        try {
            hubService.sendOperationChangeEvent(operationChangeEvent);
        }
        catch (LiquibaseException lbe) {
            logger.warning(lbe.getMessage());
            logger.warning("Unable to send Operation Change Event for operation '" + operation.getId().toString() +
                    " change set '" + changeSet.toString(false));
        }
    }

    //
    // Send an update message to Hub for this change set
    //
    private void updateHub(ChangeSet changeSet,
                           DatabaseChangeLog databaseChangeLog,
                           Database database,
                           String operationStatusType,
                           String statusMessage) {
        //
        // If not connected to Hub but we are supposed to be then show message
        //
        if (operation == null) {
            boolean realTime = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("realtime");
            if (realTime) {
                String message =
                    "Hub communication failure.\n" +
                    "The data for operation on changeset '" +
                    changeSet.getId() +
                    "' by author '" + changeSet.getAuthor() + "'\n" +
                    "was not successfully recorded in your Liquibase Hub project";
                Scope.getCurrentScope().getUI().sendMessage(message);
                logger.info(message);
            }
            return;
        }
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
        operationChangeEvent.setOperationStatusType(operationStatusType);
        operationChangeEvent.setStatusMessage(statusMessage.length() <= 255 ? statusMessage : statusMessage.substring(0,255));
        operationChangeEvent.setGeneratedSql(sqlArray);
        operationChangeEvent.setOperation(operation);
        operationChangeEvent.setLogsTimestamp(new Date());
        operationChangeEvent.setLogs("LOGS");

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
}