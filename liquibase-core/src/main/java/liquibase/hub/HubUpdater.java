package liquibase.hub;

import liquibase.*;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ListVisitor;
import liquibase.changelog.visitor.RollbackListVisitor;
import liquibase.command.CommandResults;
import liquibase.exception.CommandExecutionException;
import liquibase.command.CommandScope;
import liquibase.command.core.RegisterChangelogCommandStep;
import liquibase.command.core.InternalSyncHubCommandStep;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.core.DeprecatedConfigurationValueProvider;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.hub.model.*;
import liquibase.integration.IntegrationDetails;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
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

    private static final String SEPARATOR_LINE = "\n----------------------------------------------------------------------\n";

    public HubUpdater(Date startTime, DatabaseChangeLog changeLog, Database database) {
        this.startTime = startTime;
        this.changeLog = changeLog;
        this.database = database;
    }

    /**
     * This method performs a syncHub and returns a new Operation instance
     * If there is an error or the Hub is not available it returns null
     *
     * @param operationType     Operation type (UPDATE or ROLLBACK)
     * @param connection        Connection for this operation
     * @param changeLogFile     Path to DatabaseChangelog for this operation
     * @param contexts          Contexts to use for filtering
     * @param labelExpression   Labels to use for filtering
     * @param changeLogIterator Iterator to use for going through change sets
     * @return Operation             Valid Operation object or null
     * @throws LiquibaseHubException Thrown by HubService
     * @throws DatabaseException     Thrown by Liquibase core
     * @throws LiquibaseException    Thrown by Liquibase core
     */
    public Operation preUpdateHub(String operationType,
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

        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        HubChangeLog hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLog.getChangeLogId()), "DELETED");
        if (hubChangeLog.isDeleted()) {
            //
            // Complain and stop the operation
            //
            String message =
                "\n" +
                "The operation did not complete and will not be reported to Hub because the\n" +  "" +
                "registered changelog has been deleted by someone in your organization.\n" +
                "Learn more at http://hub.liquibase.com";
            Scope.getCurrentScope().getLog(HubUpdater.class).warning(message);
            throw new LiquibaseHubException(message);
        }

        //
        // Perform syncHub
        //
        syncHub(changeLogFile, changeLog, connection.getId());

        //
        // Load up metadata for database/driver version
        //
        loadDatabaseMetadata();

        //
        // Send the START operation event
        //
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
        if (operationType.equalsIgnoreCase("ROLLBACK")) {
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
            // Check to see if the changelog has been deactivated
            //
            final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
            final HubChangeLog hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLog.getChangeLogId()));
            if (hubChangeLog.isInactive()) {
                String message =
                    "\n" +
                        "The command completed and reported to Hub, but changelog '" + hubChangeLog.getName() + "' has been deactivated by someone in your organization.\n" +
                        "To synchronize your changelog, checkout the latest from source control or run \"deactivatechangelog\".\n" +
                        "After that, commands run against this changelog will not be reported to Hub until \"registerchangelog\" is run again.\n"  +
                        "Learn more at http://hub.liquibase.com";
                Scope.getCurrentScope().getLog(HubUpdater.class).warning(message);
                Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
            }

            //
            // Send the COMPLETE operation event
            // Capture the Liquibase Hub log level to use for filtering
            //
            Level currentLevel = HubConfiguration.LIQUIBASE_HUB_LOGLEVEL.getCurrentValue();

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
                        .setLogMessage(bufferLog.getLogAsString(currentLevel))
                                    .setTimestampLog(startTime)
                    )
            );

            //
            // Show the report link if this is an active changelog
            //
            if (hubChangeLog.isActive()) {
                showOperationReportLink(updateOperation, hubService);
            }

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
                        "After that, commands run against this changelog will not be reported to Hub until \"registerchangelog\" is run again.\n"  +
                        "Learn more at http://hub.liquibase.com";
                Scope.getCurrentScope().getLog(HubUpdater.class).warning(message);
                Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
            }

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
                        .setLogMessage(bufferLog.getLogAsString(currentLevel))
                    )
            );

            //
            // Show the report link if this is an active changelog
            //
            if (hubChangeLog.isActive()) {
                showOperationReportLink(updateOperation, hubService);
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

    public void syncHub(String changeLogFile, DatabaseChangeLog databaseChangeLog, UUID hubConnectionId) throws CommandExecutionException {
        final CommandScope syncHub = new CommandScope("internalSyncHub")
                .addArgumentValue(InternalSyncHubCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(InternalSyncHubCommandStep.URL_ARG, database.getConnection().getURL())
                .addArgumentValue(InternalSyncHubCommandStep.HUB_CONNECTION_ID_ARG, hubConnectionId)
                .addArgumentValue(InternalSyncHubCommandStep.DATABASE_ARG, database)
                .addArgumentValue(InternalSyncHubCommandStep.FAIL_IF_OFFLINE_ARG, false);

        try {
            syncHub.execute();
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
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        if (!hubService.isOnline()) {
            return;
        }

        //
        // Do not try to register if
        //   1.  We have a key already OR
        //   2.  We have a changeLogId already
        //
        if (!StringUtil.isEmpty(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue()) ||
                changeLog.getChangeLogId() != null) {
            return;
        }

        //
        // Prompt user to connect with Hub
        // Release the lock before prompting
        //
        try {
            LockService lockService = LockServiceFactory.getInstance().getLockService(database);
            lockService.releaseLock();
        } catch (LockException e) {
            Scope.getCurrentScope().getLog(HubUpdater.class).warning(Liquibase.MSG_COULD_NOT_RELEASE_LOCK);
        }
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

        //
        // Re-lock before proceeding
        //
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        String defaultsFilePath = Scope.getCurrentScope().get("defaultsFile", String.class);
        File defaultsFile = null;
        if (defaultsFilePath != null) {
            defaultsFile = new File(defaultsFilePath);
        }
        input = input.toLowerCase();
        if (input.equals("n")) {
            //
            // Write hub.mode=off to a properties file
            //
            try {
                String message = "No operations will be reported. Simply add a liquibase.hub.apiKey setting to generate free deployment reports. Learn more at https://hub.liquibase.com";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).info(message);
                writeToPropertiesFile(defaultsFile, "\nliquibase.hub.mode=off\n");
                message = "* Updated properties file " + defaultsFile + " to set liquibase.hub.mode=off";
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
        } else {
            //
            // Consider this an email
            // Call the Hub API to create a new user
            //
            HubRegisterResponse registerResponse = null;
            try {
                registerResponse = hubService.register(input);
            } catch (LiquibaseException lhe) {
                String message = "Account creation failed for email address '" + input + "': " + lhe.getMessage() + ".\n" +
                        "No operation report will be generated.";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(HubUpdater.class).warning(message);
                return;
            }
            if (registerResponse == null) {
                String message = "Account creation failed for email address '" + input + "'.\n" +
                        "No operation report will be generated.";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(HubUpdater.class).warning(message);
                return;
            }
            String message = null;
            try {
                //
                // Update the properties file
                //
                writeToPropertiesFile(defaultsFile, "\nliquibase.hub.apiKey=" + registerResponse.getApiKey() + "\n");

                //
                // If there is no liquibase.hub.mode setting then add one with value 'all'
                // Do not update liquibase.hub.mode if it is already set
                //
                ConfiguredValue<HubConfiguration.HubMode> hubModeProperty = HubConfiguration.LIQUIBASE_HUB_MODE.getCurrentConfiguredValue();
                if (hubModeProperty.wasDefaultValueUsed()) {
                    writeToPropertiesFile(defaultsFile, "\nliquibase.hub.mode=all\n");
                    message = "* Updated properties file " + defaultsFile + " to set liquibase.hub properties";
                    Scope.getCurrentScope().getUI().sendMessage(message);
                    Scope.getCurrentScope().getLog(getClass()).info(message);
                } else {
                    message = "* Updated the liquibase.hub.apiKey property.";
                    String message2 = "The liquibase.hub.mode is already set to " + hubModeProperty.getValue() + ". It will not be updated.";
                    Scope.getCurrentScope().getUI().sendMessage(message);
                    Scope.getCurrentScope().getUI().sendMessage(message2);
                    Scope.getCurrentScope().getLog(getClass()).warning(message);
                    Scope.getCurrentScope().getLog(getClass()).warning(message2);
                }

                //
                // register the changelog
                // Update the API key in HubConfiguration
                //
                message = "* Registering changelog file " + changeLogFile + " with Hub";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).info(message);
                DeprecatedConfigurationValueProvider.setData(HubConfiguration.LIQUIBASE_HUB_API_KEY, registerResponse.getApiKey());
                registerChangeLog(registerResponse.getProjectId(), changeLog, changeLogFile);

                message = "Great! Your free operation and deployment reports will be available to you after your local Liquibase commands complete.";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).info(message);
            } catch (IOException ioe) {
                message = "Unable to write information to liquibase.properties: " + ioe.getMessage() + "\n" +
                        "Please check your permissions.  No operations will be reported.";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).warning(message);
            } catch (CommandExecutionException cee) {
                message = "Unable to register changelog: " + cee.getMessage() + "\n" +
                        "No operations will be reported.";
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(getClass()).warning(message);

                System.setProperty(HubConfiguration.LIQUIBASE_HUB_API_KEY.getKey(), null);
            }
        }
    }

    //
    // Write the string to a properties file
    //
    private void writeToPropertiesFile(File defaultsFile, String stringToWrite) throws IOException {
        String encoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(defaultsFile, "rw")) {
            randomAccessFile.seek(defaultsFile.length());
            randomAccessFile.write(stringToWrite.getBytes(encoding));
        }
    }

    //
    // Register the specified changelog
    //
    private void registerChangeLog(UUID hubProjectId, DatabaseChangeLog changeLog, String changeLogFile)
            throws LiquibaseException, CommandExecutionException {

        CommandScope registerChangeLogCommand = new CommandScope("registerChangeLog");
        registerChangeLogCommand
            .addArgumentValue(RegisterChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
        try {
            if (hubProjectId != null) {
                try {
                    registerChangeLogCommand.addArgumentValue(RegisterChangelogCommandStep.HUB_PROJECT_ID_ARG, hubProjectId);
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
        // Execute registerChangeLog and reset the changeLog ID
        //
        CommandResults results = registerChangeLogCommand.execute();
        String registerChangeLogId = results.getResult(RegisterChangelogCommandStep.REGISTERED_CHANGELOG_ID);
        if (registerChangeLogId != null) {
            changeLog.setChangeLogId(registerChangeLogId);
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
