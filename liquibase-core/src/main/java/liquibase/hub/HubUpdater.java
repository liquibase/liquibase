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
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.CommandResult;
import liquibase.command.core.RegisterChangeLogCommand;
import liquibase.command.core.SyncHubCommand;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.hub.model.*;
import liquibase.integration.IntegrationDetails;
import liquibase.logging.core.BufferedLogService;
import liquibase.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * This class handles updating Hub during Liquibase operations
 */
public class HubUpdater {
    private final Date startTime;
    private final DatabaseChangeLog changeLog;
    private final Database database;

    public HubUpdater(Date startTime, DatabaseChangeLog changeLog, Database database) {
        this.startTime = startTime;
        this.changeLog = changeLog;
        this.database = database;
    }

    /**
     * This method performs a syncHub and returns a new Operation instance
     * If there is an error or the Hub is not available it returns null
     *
     * @param  operationType         Operation type (UPDATE or ROLLBACK)
     * @param  database              Database object for connection
     * @param  connection            Connection for this operation
     * @param  changeLogFile         Path to DatabaseChangelog for this operatoin
     * @param  contexts              Contexts to use for filtering
     * @param  abelExpression        Labels to use for filtering
     * @param  changeLogIterator     Iterator to use for going through change sets
     * @return Operation             Valid Operation object or null
     * @throws LiquibaseHubException Thrown by HubService
     * @throws DatabaseException     Thrown by Liquibase core
     * @throws LiquibaseException    Thrown by Liquibase core
     */
    public Operation preUpdateHub(String operationType,
                                  Database database,
                                  Connection connection,
                                  String changeLogFile,
                                  Contexts contexts,
                                  LabelExpression labelExpression,
                                  ChangeLogIterator changeLogIterator)
        throws LiquibaseHubException, DatabaseException, LiquibaseException, SQLException {
        //
        // If our current Executor is a LoggingExecutor then just return since we will not update Hub
        //
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (executor instanceof LoggingExecutor) {
            return null;
        }
        if (hubIsNotAvailable(changeLog.getChangeLogId())) {
            return null;
        }

        //
        // Perform syncHub
        //
        syncHub(changeLogFile, database, changeLog, connection.getId());

        //
        // Load up metadata for database/driver version
        //
        loadDatabaseMetadata(database);

        //
        // Send the START operation event
        //
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        final HubChangeLog hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLog.getChangeLogId()));
        Operation updateOperation = hubService.createOperation(operationType, hubChangeLog, connection);
        try {
            hubService.sendOperationEvent(updateOperation, new OperationEvent()
                .setEventType("START")
                .setStartDate(startTime)
                .setOperationEventStatus(
                    new OperationEvent.OperationEventStatus()
                        .setOperationEventStatusType("PASS")
                        .setStatusMessage("Update operation started successfully")
                )
            );
        } catch (LiquibaseException e) {
            Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
        }

        //
        // Send the list of change sets which will be updated/rolled back
        // If the operation type is DROPALL then we send no changes
        //
        ListVisitor listVisitor;
        if (operationType.toUpperCase().equals("ROLLBACK")) {
            listVisitor = new RollbackListVisitor();
        } else {
            listVisitor = new ListVisitor();
        }

        changeLogIterator.run(listVisitor, new RuntimeEnvironment(database, contexts, labelExpression));
        List<ChangeSet> operationChangeSets = listVisitor.getSeenChangeSets();
        OperationChange operationChange = new OperationChange();
        for (ChangeSet operationChangeSet : operationChangeSets) {
            operationChange.getChangeSets().add(operationChangeSet);
        }
        operationChange.setProject(hubChangeLog.getProject());
        operationChange.setOperation(updateOperation);
        try {
            hubService.sendOperationChanges(operationChange);
        } catch (LiquibaseException e) {
            Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
        }
        return updateOperation;
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
            if (updateOperation == null || hubIsNotAvailable(changeLog.getChangeLogId())) {
                return;
            }

            //
            // Send the COMPLETE operation event
            //
            final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
            hubService.sendOperationEvent(updateOperation, new OperationEvent()
                .setEventType("COMPLETE")
                .setStartDate(startTime)
                .setEndDate(new Date())
                .setOperationEventStatus(
                    new OperationEvent.OperationEventStatus()
                        .setOperationEventStatusType("PASS")
                        .setStatusMessage("Update operation completed successfully")
                )
                .setOperationEventLog(
                    new OperationEvent.OperationEventLog()
                        .setLogMessage(bufferLog.getLogAsString(Level.INFO))
                        .setTimestampLog(startTime)
                )
            );

            //
            // Show the report link
            //
            showOperationReportLink(updateOperation, hubService);

        } catch (LiquibaseException e) {
            Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
        }
    }

    /**
     * Handle Hub exceptions thrown during the operation
     *
     * @param updateOperation          Operation object used during update
     * @param bufferLog                Log output
     * @param originalExceptionMessage Exception thrown by the operation
     */
    public void postUpdateHubExceptionHandling(Operation updateOperation,
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
            if (updateOperation == null || hubIsNotAvailable(changeLog.getChangeLogId())) {
                return;
            }
            final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
            hubService.sendOperationEvent(updateOperation, new OperationEvent()
                .setEventType("COMPLETE")
                .setStartDate(startTime)
                .setEndDate(new Date())
                .setOperationEventStatus(
                    new OperationEvent.OperationEventStatus()
                        .setOperationEventStatusType("FAIL")
                        .setStatusMessage("Update operation completed with errors")
                )
                .setOperationEventLog(
                    new OperationEvent.OperationEventLog()
                        .setLogMessage(bufferLog.getLogAsString(Level.INFO))
                )
            );

            //
            // Show the report link
            //
            showOperationReportLink(updateOperation, hubService);

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

    public void syncHub(String changeLogFile, Database database, DatabaseChangeLog databaseChangeLog, UUID hubConnectionId) {
        final SyncHubCommand syncHub = (SyncHubCommand) CommandFactory.getInstance().getCommand("syncHub");
        syncHub.setChangeLogFile(changeLogFile);
        syncHub.setUrl(database.getConnection().getURL());
        syncHub.setHubConnectionId(hubConnectionId != null ? Objects.toString(hubConnectionId) : null);
        syncHub.setDatabase(database);
        syncHub.setFailIfOnline(false);

        try {
            syncHub.configure(Collections.singletonMap("changeLog", databaseChangeLog));
            final CommandResult commandResult = syncHub.execute();
            if (!commandResult.succeeded) {
                Scope.getCurrentScope().getLog(getClass()).warning("Liquibase Hub sync failed: " + commandResult.message);
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Liquibase Hub sync failed: " + e.getMessage(), e);
        }
    }

    /**
     * Automatically register the current user with Hub
     *
     * @param changeLogFile ChangeLog path for this operation
     * @throws LiquibaseException        Thrown if registration fails
     * @throws CommandExecutionException Thrown if registerChangeLog fails
     */
    public void register(String changeLogFile)
        throws LiquibaseException, CommandExecutionException {
        //
        // If our current Executor is a LoggingExecutor then just return since we will not update Hub
        //
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (executor instanceof LoggingExecutor) {
          return;
        }

        //
        // Just return if Hub is off
        //
        HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        if (!hubService.isOnline()) {
            return;
        }

        //
        // Do not try to register if
        //   1.  We have a key already OR
        //   2.  We have a changeLogId already
        //
        if (!StringUtil.isEmpty(hubConfiguration.getLiquibaseHubApiKey()) ||
            changeLog.getChangeLogId() != null) {
            return;
        }

        //
        // Prompt user to connect with Hub
        //
        String promptString =
            "Do you want to see this operation's report in Liquibase Hub, which improves team collaboration? \n" +
                "If so, enter your email. If not, enter [N] to no longer be prompted, or [S] to skip for now, but ask again next time";
        String input = Scope.getCurrentScope().getUI().prompt(promptString, "S", (input1, returnType) -> {
            input1 = input1.trim().toLowerCase();
            if (!(input1.equals("s") || input1.equals("n") || input1.contains("@"))) {
                throw new IllegalArgumentException("Invalid input '" + input1 + "'");
            }
            return input1;
        }, String.class);

        input = input.toLowerCase();
        if (input.equals("n")) {
            //
            // Write hub.mode=off to a properties file
            //
            try {
                String message = "No operations will be reported. Simply add a liquibase.hub.apiKey setting to generate free deployment reports. Learn more at https://hub.liquibase.com";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).info(message);
                String defaultsFile = Scope.getCurrentScope().get("defaultsFile", String.class);
                writeToPropertiesFile(defaultsFile, "liquibase.hub.mode=off\n");
                message = "Updated properties file " + defaultsFile + " to set liquibase.hub.mode=off";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).info(message);
            } catch (IOException ioe) {
                String message = "Unable to write hubMode to liquibase.properties: " + ioe.getMessage();
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).warning(message);
            }
        } else if (input.equals("s")) {
            String message = "Skipping auto-registration";
            Scope.getCurrentScope().getUI().sendMessage(message);
            Scope.getCurrentScope().getLog(getClass()).warning(message);

        } else  {
            //
            // Consider this an email
            // Call the Hub API to create a new user
            //
            HubRegisterResponse registerResponse = hubService.register(input);
            if (registerResponse == null) {
                Scope.getCurrentScope().getUI().sendMessage("Unable to perform auto-registration for email address " +
                    input + ". Continuing.");
            } else {
                //
                // register the changelog and write to a properties file
                //
                String message = "Registering changelog file " + changeLogFile + " with Hub";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).info(message);
                registerChangeLog(registerResponse.getProjectId(), changeLog, changeLogFile);
                try {
                    String defaultsFile = Scope.getCurrentScope().get("defaultsFile", String.class);
                    writeToPropertiesFile(defaultsFile, "liquibase.hub.apiKey=" + registerResponse.getApiKey() + "\n");
                    writeToPropertiesFile(defaultsFile, "liquibase.hub.mode=all\n");

                    message = "Updated properties file " + defaultsFile + " to set liquibase.hub properties";
                    Scope.getCurrentScope().getUI().sendMessage(message);
                    Scope.getCurrentScope().getLog(getClass()).info(message);

                    message = "Great! Your free operation and deployment reports will be available to you after your local Liquibase commands complete.";
                    Scope.getCurrentScope().getUI().sendMessage(message);
                    Scope.getCurrentScope().getLog(getClass()).info(message);
                    hubConfiguration.setLiquibaseHubApiKey(registerResponse.getApiKey());
                } catch (IOException ioe) {
                    message = "Unable to write information to liquibase.properties: " + ioe.getMessage();
                    Scope.getCurrentScope().getUI().sendMessage(message);
                    Scope.getCurrentScope().getLog(getClass()).warning(message);
                }
            }
        }
    }

    //
    // Write the string to a properties file
    //
    private void writeToPropertiesFile(String defaultsFile, String stringToWrite) throws IOException {
        File f = new File(defaultsFile);
        String encoding = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw")) {
            randomAccessFile.seek(f.length());
            randomAccessFile.write(stringToWrite.getBytes(encoding));
        }
    }

    //
    // Register the specified changelog
    //
    private void registerChangeLog(UUID hubProjectId, DatabaseChangeLog changeLog, String changeLogFile)
        throws LiquibaseException, CommandExecutionException {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("changeLog", changeLog);
        RegisterChangeLogCommand registerChangeLogCommand = new RegisterChangeLogCommand();
        registerChangeLogCommand.configure(argsMap);
        registerChangeLogCommand.setChangeLogFile(changeLogFile);
        try {
            if (hubProjectId != null) {
                try {
                    registerChangeLogCommand.setHubProjectId(hubProjectId);
                } catch (IllegalArgumentException e) {
                    throw new LiquibaseException("The command 'RegisterChangeLog' " +
                        " failed because parameter 'hubProjectId' has invalid value '" + hubProjectId +
                        "'. Learn more at https://hub.liquibase.com");
                }
            }
        } catch (IllegalArgumentException e) {
            throw new LiquibaseException("Unexpected hubProjectId format: " + hubProjectId, e);
        }

        //
        // Execute registerChangeLog
        //
        CommandResult result = registerChangeLogCommand.execute();
        if (result.succeeded) {
            Scope.getCurrentScope().getUI().sendMessage(result.print());
        } else {
            throw new RuntimeException(result.print());
        }
    }

    //
    // Show a link to the user
    //
    private void showOperationReportLink(Operation updateOperation, HubService hubService) throws LiquibaseException {
        //
        // Send the operation report link to Hub for shortening
        //
        Connection connection = updateOperation.getConnection();

        String reportURL =
            "/organizations/" + hubService.getOrganization().getId().toString() +
                "/projects/" + connection.getProject().getId() +
                "/operations/" + updateOperation.getId().toString();


        String hubLink = hubService.shortenLink(reportURL);

        String message = "View a report of this operation at " + hubLink;
        Scope.getCurrentScope().getUI().sendMessage(message);
        Scope.getCurrentScope().getLog(getClass()).info(message);
    }

    //
    // Put database/driver version information in the details map
    //
    private void loadDatabaseMetadata(Database database) throws DatabaseException, SQLException {
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
                Integer.toString(driverMajorVersion) + "." + Integer.toString(driverMinorVersion));
            integrationDetails.setParameter("db__driverVersion", Integer.toString(driverMajorVersion) + "." + Integer.toString(driverMinorVersion));
        } else {
            integrationDetails.setParameter("db__driverVersion", "Unable to determine");
        }
        integrationDetails.setParameter("db__databaseProduct", databaseProductName);
        integrationDetails.setParameter("db__databaseVersion", databaseProductVersion);
    }
}
