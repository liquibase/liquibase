package liquibase.hub.listener;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.ChangeLogSyncListener;
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
import liquibase.logging.core.BufferedLogService;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class HubChangeExecListener extends AbstractChangeExecListener
                                   implements ChangeExecListener, ChangeLogSyncListener {
    private static final Logger logger = Scope.getCurrentScope().getLog(HubChangeExecListener.class);

    private final Operation operation;

    private final Map<ChangeSet, Date> startDateMap = new HashMap<>();

    private String rollbackScriptContents;

    private int postCount;
    private int failedToPostCount;

    private ChangeExecListener changeExecListener;

    public HubChangeExecListener(Operation operation, ChangeExecListener changeExecListener) {
        this.operation = operation;
        this.changeExecListener = changeExecListener;
    }

    public void setRollbackScriptContents(String rollbackScriptContents) {
        this.rollbackScriptContents = rollbackScriptContents;
    }

    public int getPostCount() {
        return postCount;
    }

    public int getFailedToPostCount() {
        return failedToPostCount;
    }

    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        startDateMap.put(changeSet, new Date());
        if (changeExecListener != null) {
            changeExecListener.willRun(changeSet, databaseChangeLog, database, runStatus);
        }
    }

    @Override
    public void willRun(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        startDateMap.put(changeSet, new Date());
        if (changeExecListener != null) {
            changeExecListener.willRun(change, changeSet, changeLog, database);
        }
    }

    @Override
    public void ran(ChangeSet changeSet,
                    DatabaseChangeLog databaseChangeLog,
                    Database database,
                    ChangeSet.ExecType execType) {
        String message = "PASSED::" + changeSet.getId() + "::" + changeSet.getAuthor();
        updateHub(changeSet, databaseChangeLog, database, "UPDATE", "PASS", message);
        if (changeExecListener != null) {
            changeExecListener.ran(changeSet, databaseChangeLog, database, execType);
        }
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
        if (changeExecListener != null) {
            changeExecListener.willRollback(changeSet, databaseChangeLog, database);
        }
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
        if (changeExecListener != null) {
            changeExecListener.rollbackFailed(changeSet, databaseChangeLog, database, e);
        }
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
        if (changeExecListener != null) {
            changeExecListener.rolledBack(changeSet, databaseChangeLog, database);
        }
    }

    @Override
    public void preconditionFailed(PreconditionFailedException error, PreconditionContainer.FailOption onFail) {
        if (changeExecListener != null) {
            changeExecListener.preconditionFailed(error, onFail);
        }
    }

    @Override
    public void preconditionErrored(PreconditionErrorException error, PreconditionContainer.ErrorOption onError) {
        if (changeExecListener != null) {
            changeExecListener.preconditionErrored(error, onError);
        }
    }

    @Override
    public void ran(Change change, ChangeSet changeSet, DatabaseChangeLog changeLog, Database database) {
        if (changeExecListener != null) {
            changeExecListener.ran(change, changeSet, changeLog, database);
        }
    }

    @Override
    public void runFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        updateHub(changeSet, databaseChangeLog, database, "UPDATE", "FAIL", exception.getMessage());
        if (changeExecListener != null) {
            changeExecListener.runFailed(changeSet, databaseChangeLog, database, exception);
        }
    }

    @Override
    public void markedRan(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) {
        startDateMap.put(changeSet, new Date());
        String message = "PASSED::" + changeSet.getId() + "::" + changeSet.getAuthor();
        updateHub(changeSet, databaseChangeLog, database, "SYNC", "PASS", message);
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
            HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
            String apiKey = StringUtil.trimToNull(hubConfiguration.getLiquibaseHubApiKey());
            boolean hubOn =
                    ! (LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("off"));
            if (apiKey != null && hubOn) {
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
            hubChangeLog = hubService.getHubChangeLog(UUID.fromString(databaseChangeLog.getChangeLogId()));
            if (hubChangeLog == null) {
                logger.warning("The changelog '" + databaseChangeLog.getPhysicalFilePath() + "' has not been registered with Hub");
                return;
            }
        }
        catch (LiquibaseHubException lhe) {
            logger.warning("The changelog '" + databaseChangeLog.getPhysicalFilePath() + "' has not been registered with Hub");
            return;
        }

        Date dateExecuted = new Date();

        //
        //  POST /organizations/{id}/projects/{id}/operations/{id}/change-events
        //
        OperationChangeEvent operationChangeEvent = new OperationChangeEvent();
        operationChangeEvent.setEventType("ROLLBACK");
        operationChangeEvent.setStartDate(startDateMap.get(changeSet));
        operationChangeEvent.setEndDate(dateExecuted);
        operationChangeEvent.setDateExecuted(dateExecuted);
        operationChangeEvent.setChangesetId(changeSet.getId());
        operationChangeEvent.setChangesetFilename(changeSet.getFilePath());
        operationChangeEvent.setChangesetAuthor(changeSet.getAuthor());
        List<String> sqlList = new ArrayList<>();
        try {
            if (rollbackScriptContents != null) {
                sqlList.add(rollbackScriptContents);
            }
            else if (changeSet.hasCustomRollbackChanges()) {
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
        if ("FAIL".equals(operationStatusType)) {
            operationChangeEvent.setLogs(statusMessage);
        }
        else {
            String logs = getCurrentLog();
            if (! StringUtil.isEmpty(logs)) {
                operationChangeEvent.setLogs(logs);
            }
            else {
                operationChangeEvent.setLogs(statusMessage);
            }
        }
        operationChangeEvent.setLogsTimestamp(new Date());
        operationChangeEvent.setProject(hubChangeLog.getProject());
        operationChangeEvent.setOperation(operation);

        try {
            hubService.sendOperationChangeEvent(operationChangeEvent);
            postCount++;
        }
        catch (LiquibaseException lbe) {
            logger.warning(lbe.getMessage(), lbe);
            logger.warning("Unable to send Operation Change Event for operation '" + operation.getId().toString() +
                    " change set '" + changeSet.toString(false));
        }
    }

    private String getCurrentLog() {
        BufferedLogService bufferedLogService =
           Scope.getCurrentScope().get(BufferedLogService.class.getName(), BufferedLogService.class);
        if (bufferedLogService != null) {
            return bufferedLogService.getLogAsString(Level.INFO);
        }
        return null;
    }

    //
    // Send an update message to Hub for this change set
    //
    private void updateHub(ChangeSet changeSet,
                           DatabaseChangeLog databaseChangeLog,
                           Database database,
                           String eventType,
                           String operationStatusType,
                           String statusMessage) {
        //
        // If not connected to Hub but we are supposed to be then show message
        //
        if (operation == null) {
            HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
            String apiKey = StringUtil.trimToNull(hubConfiguration.getLiquibaseHubApiKey());
            boolean hubOn =
                ! (LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("off"));
            if (apiKey != null && hubOn) {
                String message;
                if (databaseChangeLog.getChangeLogId() == null) {
                    message = "The changelog '" + databaseChangeLog.getPhysicalFilePath() + "' has not been registered with Liquibase Hub.\n" +
                            "To register the changelog with your Hub Project run 'liquibase registerChangeLog'.\n" +
                            "Learn more at https://hub.liquibase.com.";
                }
                else {
                    message = "The changelog file specified is not registered with any Liquibase Hub project, so the results will not be recorded in Liquibase Hub.\n" +
                            "To register the changelog with your Hub Project run 'liquibase registerChangeLog'.\n" +
                            "Learn more at https://hub.liquibase.com.";
                }
                Scope.getCurrentScope().getUI().sendMessage(message);
                logger.info(message);
            }
            return;
        }
        HubChangeLog hubChangeLog;
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        try {
            hubChangeLog = hubService.getHubChangeLog(UUID.fromString(databaseChangeLog.getChangeLogId()));
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
        //  Do not send generated SQL or changeset body for changeLogSync operation
        //
        OperationChangeEvent operationChangeEvent = new OperationChangeEvent();
        List<String> sqlList = new ArrayList<>();
        if (! eventType.equals("SYNC")) {
            List<Change> changes = changeSet.getChanges();
            for (Change change : changes) {
                try {
                    Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(change, database);
                    for (Sql sql : sqls) {
                        sqlList.add(sql.toSql());
                    }
                }
                catch (Exception e) {
                    logger.warning("Unable to generate SQL for Hub failure message: " + e.getMessage());
                }
            }
            String[] sqlArray = new String[sqlList.size()];
            sqlArray = sqlList.toArray(sqlArray);
            operationChangeEvent.setGeneratedSql(sqlArray);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChangeLogSerializer serializer = ChangeLogSerializerFactory.getInstance().getSerializer(".json");
            try {
                serializer.write(Collections.singletonList(changeSet), baos);
                operationChangeEvent.setChangesetBody(baos.toString("UTF-8"));
            } catch (IOException ioe) {
                //
                // Just log message
                //
                logger.warning("Unable to serialize change set '" + changeSet.toString(false) + "' for Hub.");
            }
        }

        Date dateExecuted = new Date();

        String[] sqlArray = new String[sqlList.size()];
        sqlArray = sqlList.toArray(sqlArray);
        operationChangeEvent.setEventType(eventType);
        operationChangeEvent.setStartDate(startDateMap.get(changeSet));
        operationChangeEvent.setEndDate(dateExecuted);
        operationChangeEvent.setDateExecuted(dateExecuted);
        operationChangeEvent.setChangesetId(changeSet.getId());
        operationChangeEvent.setChangesetFilename(changeSet.getFilePath());
        operationChangeEvent.setChangesetAuthor(changeSet.getAuthor());
        operationChangeEvent.setOperationStatusType(operationStatusType);
        operationChangeEvent.setStatusMessage(statusMessage);
        operationChangeEvent.setGeneratedSql(sqlArray);
        operationChangeEvent.setOperation(operation);
        operationChangeEvent.setLogsTimestamp(new Date());
        if ("FAIL".equals(operationStatusType)) {
            operationChangeEvent.setLogs(statusMessage);
        }
        else {
            String logs = getCurrentLog();
            if (! StringUtil.isEmpty(logs)) {
                operationChangeEvent.setLogs(logs);
            }
            else {
                operationChangeEvent.setLogs(statusMessage);
            }
        }

        operationChangeEvent.setProject(hubChangeLog.getProject());
        operationChangeEvent.setOperation(operation);
        try {
            hubService.sendOperationChangeEvent(operationChangeEvent);
            postCount++;
        }
        catch (LiquibaseException lbe) {
            logger.warning(lbe.getMessage(), lbe);
            logger.warning("Unable to send Operation Change Event for operation '" + operation.getId().toString() +
                    " change set '" + changeSet.toString(false));
            failedToPostCount++;
        }
    }
}
