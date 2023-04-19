package liquibase.hub;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ListVisitor;
import liquibase.changelog.visitor.RollbackListVisitor;
import liquibase.command.CommandScope;
import liquibase.command.core.InternalSyncHubCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.hub.model.*;
import liquibase.integration.IntegrationDetails;
import liquibase.logging.core.BufferedLogService;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This class handles updating Hub during Liquibase operations
 */
public class HubUpdater {
    private final Date startTime;
    private final DatabaseChangeLog changeLog;
    private final Database database;

    private static final String SEPARATOR_LINE = "\n----------------------------------------------------------------------\n";
    final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();

    public HubUpdater(Date startTime, DatabaseChangeLog changeLog, Database database) {
        this.startTime = startTime;
        this.changeLog = changeLog;
        this.database = database;
    }

    public HubUpdater(Date startTime, Database database) {
        this.startTime = startTime;
        this.database = database;
        this.changeLog = null;
    }

    /**
     * This method performs a syncHub and returns a new Operation instance
     * If there is an error or the Hub is not available it returns null
     *
     * @param operationType Operation type (UPDATE or ROLLBACK)
     * @param connection    Connection for this operation
     * @return Operation        Valid Operation object or null
     * @throws LiquibaseHubException Thrown by HubService
     * @throws DatabaseException     Thrown by Liquibase core
     * @throws LiquibaseException    Thrown by Liquibase core
     */
    public Operation preUpdateHub(String operationType, String operationCommand, Connection connection)
            throws LiquibaseException, SQLException {
        if (connection == null || connection.getProject() == null) {
            return null;
        }
        return this.preUpdateHub(operationType, operationCommand, connection, null, null, null, null);
    }

    /**
     * This method performs a syncHub and returns a new Operation instance
     * If there is an error or the Hub is not available it returns null
     *
     * @param operationType     Operation type (UPDATE, ROLLBACK, or CHANGELOGSYNC)
     * @param operationCommand  Specific command which is executing (update, update-count, etc.)
     * @param connection        Connection for this operation
     * @param changeLogFile     Path to DatabaseChangelog for this operation
     * @param contexts          Contexts to use for filtering
     * @param labelExpression   Labels to use for filtering
     * @param changeLogIterator Iterator to use for going through changesets
     * @return Operation        Valid Operation object or null
     * @throws LiquibaseHubException Thrown by HubService
     * @throws DatabaseException     Thrown by Liquibase core
     * @throws LiquibaseException    Thrown by Liquibase core
     */
    public Operation preUpdateHub(String operationType,
                                  String operationCommand,
                                  Connection connection,
                                  String changeLogFile,
                                  Contexts contexts,
                                  LabelExpression labelExpression,
                                  ChangeLogIterator changeLogIterator)
            throws LiquibaseHubException, DatabaseException, LiquibaseException, SQLException {

        // If our current Executor is a LoggingExecutor then just return since we will not update Hub
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (executor instanceof LoggingExecutor) {
            return null;
        }
        HubChangeLog hubChangeLog = getHubChangeLog();

        // Perform syncHub
        syncHub(changeLogFile, connection);

        // Load up metadata for database/driver version
        loadDatabaseMetadata();

        // Send the START operation event
        Operation operation = sendStartOperationEvent(operationType, operationCommand, connection, hubChangeLog);

        ListVisitor listVisitor;
        if (operationType.equalsIgnoreCase("ROLLBACK")) {
            listVisitor = new RollbackListVisitor();
        } else {
            listVisitor = new ListVisitor();
        }
        OperationChange operationChange = new OperationChange();
        populateOperationChange(contexts, labelExpression, changeLogIterator, listVisitor, operationChange);
        populateProject(connection, hubChangeLog, operationChange);
        operationChange.setOperation(operation);

        try {
            hubService.sendOperationChanges(operationChange);
        } catch (LiquibaseException e) {
            Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
        }
        if (operation != null) {
            Operation.OperationStatus operationStatus = new Operation.OperationStatus();
            operationStatus.setOperationStatusType(operationType);
            operation.setOperationStatus(operationStatus);
        }
        return operation;
    }

    private void populateOperationChange(Contexts contexts, LabelExpression labelExpression, ChangeLogIterator changeLogIterator, ListVisitor listVisitor, OperationChange operationChange) throws LiquibaseException {
        if (changeLogIterator != null) {
            changeLogIterator.run(listVisitor, new RuntimeEnvironment(database, contexts, labelExpression));
            List<ChangeSet> operationChangeSets = listVisitor.getSeenChangeSets();
            for (ChangeSet operationChangeSet : operationChangeSets) {
                operationChange.getChangeSets().add(operationChangeSet);
            }
        }
    }

    private void populateProject(Connection connection, HubChangeLog hubChangeLog, OperationChange operationChange) {
        if (hubChangeLog == null) {
            operationChange.setProject(connection.getProject());
        } else {
            operationChange.setProject(hubChangeLog.getProject());
        }
    }

    private Operation sendStartOperationEvent(String operationType, String operationCommand, Connection connection, HubChangeLog hubChangeLog) throws LiquibaseHubException {
        Operation updateOperation = hubService.createOperation(operationType, operationCommand, hubChangeLog, connection);
        try {
            hubService.sendOperationEvent(updateOperation, new OperationEvent()
                    .setEventType("START")
                    .setStartDate(startTime)
                    .setOperationEventStatus(
                            new OperationEvent.OperationEventStatus()
                                    .setOperationEventStatusType("PASS")
                                    .setStatusMessage(String.format("%s operation started successfully", operationType))
                    )
            );
        } catch (LiquibaseException e) {
            Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
        }
        return updateOperation;
    }

    private HubChangeLog getHubChangeLog() throws LiquibaseHubException {
        HubChangeLog hubChangeLog = null;
        if (changeLog != null) {
            if (hubIsNotAvailable(changeLog.getChangeLogId())) {
                return null;
            }
            hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLog.getChangeLogId()), "DELETED");
            if (hubChangeLog.isDeleted()) {
                // Complain and stop the operation
                String message =
                        "\n" +
                                "The operation did not complete and will not be reported to Hub because the\n" + "" +
                                "registered changelog has been deleted by someone in your organization.\n" +
                                "Learn more at http://hub.liquibase.com";
                Scope.getCurrentScope().getLog(HubUpdater.class).warning(message);
                throw new LiquibaseHubException(message);
            }
        }
        return hubChangeLog;
    }

    /**
     * Update the Hub after the operation
     *
     * @param updateOperation Operation object used in the update
     * @param bufferLog       Log output
     */
    public void postUpdateHub(Operation updateOperation, BufferedLogService bufferLog) {
        try {
            //
            // If our current Executor is a LoggingExecutor then just return since we will not update Hub
            //
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            if (executor instanceof LoggingExecutor) {
                return;
            }
            if (updateOperation == null || (changeLog != null && hubIsNotAvailable(changeLog.getChangeLogId()))) {
                return;
            }

            //
            // Check to see if the changelog has been deactivated
            //
            if (changeLog != null) {
                final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
                final HubChangeLog hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLog.getChangeLogId()));
                if (hubChangeLog.isInactive()) {
                    String message =
                            "\n" +
                                    "The command completed and reported to Hub, but changelog '" + hubChangeLog.getName() + "' has been deactivated by someone in your organization.\n" +
                                    "To synchronize your changelog, checkout the latest from source control or run \"deactivatechangelog\".\n" +
                                    "After that, commands run against this changelog will not be reported to Hub until \"registerchangelog\" is run again.\n" +
                                    "Learn more at http://hub.liquibase.com";
                    Scope.getCurrentScope().getLog(HubUpdater.class).warning(message);
                    Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
                }
                sendCompleteOperationEvent(updateOperation, bufferLog);
                // Show the report link if this is an active changelog
                if (hubChangeLog.isActive()) {
                    showOperationReportLink(updateOperation, hubService);
                }
            } else {
                sendCompleteOperationEvent(updateOperation, bufferLog);
                showOperationReportLink(updateOperation, hubService);

            }
        } catch (LiquibaseException e) {
            Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
        }
    }

    //
    // Send the COMPLETE operation event
    // Capture the Liquibase Hub log level to use for filtering
    //
    private void sendCompleteOperationEvent(Operation updateOperation, BufferedLogService bufferLog) throws LiquibaseException {
        Level currentLevel = HubConfiguration.LIQUIBASE_HUB_LOGLEVEL.getCurrentValue();

        hubService.sendOperationEvent(updateOperation, new OperationEvent()
                .setEventType("COMPLETE")
                .setStartDate(startTime)
                .setEndDate(new Date())
                .setOperationEventStatus(
                        new OperationEvent.OperationEventStatus()
                                .setOperationEventStatusType("PASS")
                                .setStatusMessage(String.format("%s operation completed successfully", updateOperation.getOperationStatus().getOperationStatusType()))
                )
                .setOperationEventLog(
                        new OperationEvent.OperationEventLog()
                                .setLogMessage(bufferLog.getLogAsString(currentLevel))
                                .setTimestampLog(startTime)
                )
        );
    }

    /**
     * Handle Hub exceptions thrown during the operation
     *
     * @param operation                Operation object
     * @param bufferLog                Log output
     * @param originalExceptionMessage Exception thrown by the operation
     */
    public void postUpdateHubExceptionHandling(Operation operation,
                                               BufferedLogService bufferLog,
                                               String originalExceptionMessage) {
        try {
            //
            // If our current Executor is a LoggingExecutor then just return since we will not update Hub
            //
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            if (executor instanceof LoggingExecutor) {
                return;
            }

            //
            // Not a valid Hub connection
            // Just go back
            //
            if (operation == null || hubIsNotAvailable(changeLog.getChangeLogId())) {
                return;
            }


            //
            // Capture the current log level to use for filtering
            //
            Level currentLevel = HubConfiguration.LIQUIBASE_HUB_LOGLEVEL.getCurrentValue();

            //
            // Check to see if the changelog has been deactivated
            //
            final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
            final HubChangeLog hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLog.getChangeLogId()));
            if (hubChangeLog.isInactive()) {
                String message =
                        "\n" +
                                "The command completed and reported to Hub, but changelog '" + hubChangeLog.getName() + "' has been deactivated by someone in your organization.\n" +
                                "To synchronize your changelog, checkout the latest from source control or run \"deactivatechangelog\".\n" +
                                "After that, commands run against this changelog will not be reported to Hub until \"registerchangelog\" is run again.\n" +
                                "Learn more at http://hub.liquibase.com";
                Scope.getCurrentScope().getLog(HubUpdater.class).warning(message);
                Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
            }

            hubService.sendOperationEvent(operation, new OperationEvent()
                    .setEventType("COMPLETE")
                    .setStartDate(startTime)
                    .setEndDate(new Date())
                    .setOperationEventStatus(
                            new OperationEvent.OperationEventStatus()
                                    .setOperationEventStatusType("FAIL")
                                    .setStatusMessage(String.format("%s operation completed with errors", operation.getOperationStatus().getOperationStatusType()))
                    )
                    .setOperationEventLog(
                            new OperationEvent.OperationEventLog()
                                    .setLogMessage(bufferLog.getLogAsString(currentLevel))
                    )
            );

            //
            // Show the report link if this is an active changelog
            //
            if (hubChangeLog.isActive()) {
                showOperationReportLink(operation, hubService);
            }

        } catch (LiquibaseException serviceException) {
            Scope.getCurrentScope().getLog(getClass()).warning(originalExceptionMessage, serviceException);
        }
    }

    /**
     * Determine if the Hub is not available
     *
     * @param changeLogId Changelog ID
     * @return boolean
     */
    public boolean hubIsNotAvailable(String changeLogId) {
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        return !hubService.isOnline() || changeLogId == null;
    }

    public void syncHub(String changeLogFile, Connection hubConnection) throws CommandExecutionException {
        //
        // We pass in a setting of CONTINUE IF_BOTH_CONNECTION_AND_PROJECT_ID_SET_ARG=true
        // to tell syncHub to not complain when both connectionID and projectID
        // are set.
        //
        UUID hubConnectionId = (hubConnection != null ? hubConnection.getId() : null);
        UUID hubProjectId = (hubConnection != null && hubConnection.getProject() != null ? hubConnection.getProject().getId() : null);
        final CommandScope syncHub = new CommandScope("internalSyncHub")
                .addArgumentValue(InternalSyncHubCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(InternalSyncHubCommandStep.URL_ARG, database.getConnection().getURL())
                .addArgumentValue(InternalSyncHubCommandStep.HUB_CONNECTION_ID_ARG, hubConnectionId)
                .addArgumentValue(InternalSyncHubCommandStep.HUB_PROJECT_ID_ARG, hubProjectId)
                .addArgumentValue(InternalSyncHubCommandStep.CONTINUE_IF_CONNECTION_AND_PROJECT_ID_BOTH_SET_ARG, true)
                .addArgumentValue(InternalSyncHubCommandStep.DATABASE_ARG, database)
                .addArgumentValue(InternalSyncHubCommandStep.FAIL_IF_OFFLINE_ARG, false);

        try {
            syncHub.execute();
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Liquibase Hub sync failed: " + e.getMessage(), e);
        }
    }

    //
    // Show a link to the user
    //
    private void showOperationReportLink(Operation updateOperation, HubService hubService)
            throws LiquibaseException {
        //
        // Send the operation report link to Hub for shortening
        //
        Connection connection = updateOperation.getConnection();

        String reportURL =
                "/organizations/" + hubService.getOrganization().getId().toString() +
                        "/projects/" + connection.getProject().getId() +
                        "/operations/" + updateOperation.getId().toString();


        String hubLink = hubService.shortenLink(reportURL);
        // View a report of this operation at http://localhost:8888/r/8SqckqSvKm
        // * IMPORTANT: New users of Hub first need to Sign In to their account
        // "with the one-time password, using <email> as their username.
        String message = SEPARATOR_LINE;
        message += "View a report of this operation at " + hubLink + "\n";
        message += "* IMPORTANT: New users of Hub first need to Sign In to your account\n";
        message += "with the one-time password sent to your email, which also serves as\n";
        message += "your username.";
        message += SEPARATOR_LINE;
        Scope.getCurrentScope().getUI().sendMessage(message);
        Scope.getCurrentScope().getLog(getClass()).info(message);
    }

    //
    // Put database/driver version information in the details map
    //
    private void loadDatabaseMetadata() throws DatabaseException, SQLException {
        if (database.getConnection() == null) {
            return;
        }
        final IntegrationDetails integrationDetails = Scope.getCurrentScope().get("integrationDetails", IntegrationDetails.class);
        if (integrationDetails == null) {
            return;
        }
        String databaseProductName = database.getDatabaseProductName();
        String databaseProductVersion = database.getDatabaseProductVersion();
        Scope.getCurrentScope().getLog(getClass()).fine("Database product name         " + databaseProductName);
        Scope.getCurrentScope().getLog(getClass()).fine("Database product version      " + databaseProductVersion);

        DatabaseConnection connection = database.getConnection();
        if (connection instanceof JdbcConnection) {
            JdbcConnection jdbcConnection = (JdbcConnection) connection;
            java.sql.Connection conn = jdbcConnection.getUnderlyingConnection();
            int driverMajorVersion = conn.getMetaData().getDriverMajorVersion();
            int driverMinorVersion = conn.getMetaData().getDriverMinorVersion();
            Scope.getCurrentScope().getLog(getClass()).fine("Database driver version       " +
                    driverMajorVersion + "." + driverMinorVersion);
            integrationDetails.setParameter("db__driverVersion", driverMajorVersion + "." + driverMinorVersion);
        } else {
            integrationDetails.setParameter("db__driverVersion", "Unable to determine");
        }
        integrationDetails.setParameter("db__databaseProduct", databaseProductName);
        integrationDetails.setParameter("db__databaseVersion", databaseProductVersion);
    }
}
