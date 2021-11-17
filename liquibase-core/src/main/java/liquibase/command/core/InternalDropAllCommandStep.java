package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.hub.*;
import liquibase.hub.model.*;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.logging.core.BufferedLogService;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.Date;
import java.util.UUID;

@SuppressWarnings("java:S1144")
public class InternalDropAllCommandStep extends AbstractCommandStep {

    protected static final String[] COMMAND_NAME = {"internalDropAll"};
    private final Logger log = Scope.getCurrentScope().getLog(getClass());

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<DatabaseChangeLog> CHANGELOG_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<UUID> HUB_CONNECTION_ID_ARG;
    public static final CommandArgumentDefinition<UUID> HUB_PROJECT_ID_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        DATABASE_ARG = builder.argument("database", Database.class).required()
                .description("Database to drop objects in").build();
        SCHEMAS_ARG = builder.argument("schemas", CatalogAndSchema[].class)
                .description("Schemas to drop objects in").build();
        CHANGELOG_ARG = builder.argument("changelog", DatabaseChangeLog.class).build();
        CHANGELOG_FILE_ARG = builder.argument("changeFile", String.class)
                .description("The root changelog").build();
        HUB_CONNECTION_ID_ARG = builder.argument("hubConnectionId", UUID.class)
                .description("Used to identify the specific Connection in which to record or extract data at Liquibase Hub. Available in your Liquibase Hub Project at https://hub.liquibase.com.").build();
        HUB_PROJECT_ID_ARG = builder.argument("hubProjectId", UUID.class)
                .description("Used to identify the specific Project in which to record at Liquibase Hub. Available in your Liquibase Hub account at https://hub.liquibase.com.").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        BufferedLogService bufferLog = new BufferedLogService();
        validateConnectionAndProjectIdsDependingOnApiKey(commandScope);
        Operation dropAllOperation;
        LockService lockService = LockServiceFactory.getInstance().getLockService(commandScope.getArgumentValue(DATABASE_ARG));
        HubUpdater hubUpdater;

        try {
            lockService.waitForLock();

            DatabaseChangeLog changeLog;
            HubRegisterResponse hubRegisterResponse = null;
            if (StringUtil.isNotEmpty(commandScope.getArgumentValue(CHANGELOG_FILE_ARG))) {
                // Let the user know they can register for Hub
                changeLog = parseChangeLogFile(commandScope.getArgumentValue(CHANGELOG_FILE_ARG));
                hubUpdater = new HubUpdater(new Date(), changeLog, commandScope.getArgumentValue(DATABASE_ARG));
                hubRegisterResponse = hubUpdater.register(commandScope.getArgumentValue(CHANGELOG_FILE_ARG));

                // Access the HubChangeLog and check to see if we should run syncHub
                HubChangeLog hubChangeLog = getHubChangeLog(changeLog);
                checkForRegisteredChangeLog(changeLog, hubChangeLog);
            } else {
                hubUpdater = new HubUpdater(new Date(), commandScope.getArgumentValue(DATABASE_ARG));
                hubRegisterResponse = hubUpdater.register(null/*changelogFile*/);
            }
            Connection hubConnection = getHubConnection(commandScope);
            attachProjectToConnection(commandScope, hubConnection, hubRegisterResponse);

            dropAllOperation = hubUpdater.preUpdateHub("DROPALL", "drop-all", hubConnection);

            try {
                for (CatalogAndSchema schema : commandScope.getArgumentValue(SCHEMAS_ARG)) {
                    log.info("Dropping Database Objects in schema: " + schema);
                    checkLiquibaseTables(commandScope.getArgumentValue(DATABASE_ARG));
                    commandScope.getArgumentValue(DATABASE_ARG).dropDatabaseObjects(schema);
                }
            } catch (LiquibaseException liquibaseException) {
                hubUpdater.postUpdateHubExceptionHandling(dropAllOperation, bufferLog, liquibaseException.getMessage());
                return;
            }
            final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
            String apiKey = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue());
            if (apiKey != null && hubServiceFactory.isOnline()) {
                hubUpdater.syncHub(commandScope.getArgumentValue(CHANGELOG_FILE_ARG), hubConnection);
                hubUpdater.postUpdateHub(dropAllOperation, bufferLog);
            }
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            lockService.releaseLock();
            lockService.destroy();
            resetServices();
        }

        Scope.getCurrentScope().getUI().sendMessage("All objects dropped from " + commandScope.getArgumentValue(DATABASE_ARG).getConnection().getConnectionUserName() + "@" + commandScope.getArgumentValue(DATABASE_ARG).getConnection().getURL());
        resultsBuilder.addResult("statusCode", 0);
    }

    /**
     * Method to attach project to connection
     *
     * @param commandScope        - The primary facade used for executing commands where we can take cmd arguments
     * @param hubConnection       - It's hubConnection
     * @param hubRegisterResponse - it's response from auto registration API. It it's null we assume
     *                            that user has been registered already of we don't have connection to HUB
     * @throws LiquibaseHubException - If project can't be found and attached to connection
     */
    private void attachProjectToConnection(CommandScope commandScope, Connection hubConnection, HubRegisterResponse hubRegisterResponse) throws LiquibaseHubException {
        String apiKey = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue());
        if (apiKey == null) {
            return;
        }
        UUID connectionId = commandScope.getArgumentValue(HUB_CONNECTION_ID_ARG);
        UUID projectId = commandScope.getArgumentValue(HUB_PROJECT_ID_ARG);
        if (hubRegisterResponse != null) {
            projectId = hubRegisterResponse.getProjectId();
        }
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        Project project;
        if (projectId != null) {
            project = hubService.getProject(projectId);
            hubConnection.setProject(project);
        } else {
            project = hubService.findProjectByConnectionIdOrJdbcUrl(connectionId, hubConnection.getJdbcUrl());
        }
        hubConnection.setProject(project);

        if (hubConnection.getProject() == null) {
            String message = "Operation will not be sent to Liquibase Hub. Please specify --hubProjectId=<id> or --hubConnectionId=<id>";
            Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
            log.warning(message);
        }

    }

    private Connection getHubConnection(CommandScope commandScope) {
        String apiKey = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue());
        if (apiKey == null) {
            return null;
        }
        Database database = commandScope.getArgumentValue(DATABASE_ARG);
        DatabaseConnection dbConnection = database.getConnection();
        Connection connection = new Connection();
        connection.setId(commandScope.getArgumentValue(HUB_CONNECTION_ID_ARG));
        connection.setJdbcUrl(dbConnection.getURL());
        return connection;
    }

    private void validateConnectionAndProjectIdsDependingOnApiKey(CommandScope commandScope) throws CommandExecutionException {
        UUID connectionId = commandScope.getArgumentValue(HUB_CONNECTION_ID_ARG);
        UUID projectId = commandScope.getArgumentValue(HUB_PROJECT_ID_ARG);
        String apiKey = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue());
        if (apiKey == null) {
            if (connectionId == null && projectId == null) {
                return;
            }
            throw new CommandExecutionException("No valid Hub API Key detected. Please add liquibase.hub.apikey to \n" +
                    "defaults file or pass --hub-api-key=<yourkey> on the command line.");
        }
    }

    //
    // Return a HubChangeLog object if available
    // If not available then return null
    // If the HubChangeLog has been deleted then throw
    // a LiquibaseHubException
    //
    private HubChangeLog getHubChangeLog(DatabaseChangeLog changeLog) throws LiquibaseHubException {
        String apiKey = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue());
        HubConfiguration.HubMode hubMode = HubConfiguration.LIQUIBASE_HUB_MODE.getCurrentValue();
        String changeLogId = changeLog.getChangeLogId();
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (apiKey == null || hubMode == HubConfiguration.HubMode.OFF || !hubServiceFactory.isOnline()) {
            return null;
        }
        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        HubChangeLog hubChangeLog = (changeLogId != null ? service.getHubChangeLog(UUID.fromString(changeLogId), "*") : null);
        if (hubChangeLog == null) {
            return null;
        }

        //
        // Stop the operation if the HubChangeLog has been deleted
        //
        if (hubChangeLog.isDeleted()) {
            //
            // Complain and stop the operation
            //
            String message =
                    "\n" +
                            "The operation did not complete and will not be reported to Hub because the\n" + "" +
                            "registered changelog has been deleted by someone in your organization.\n" +
                            "Learn more at http://hub.liquibase.com";
            throw new LiquibaseHubException(message);
        }
        return hubChangeLog;
    }

    private void checkForRegisteredChangeLog(DatabaseChangeLog changeLog, HubChangeLog hubChangeLog) {
        String changeLogId = changeLog.getChangeLogId();
        if (changeLogId != null && hubChangeLog != null) {
            return;
        }
        String message =
                "The changelog file specified is not registered with any Liquibase Hub project,\n" +
                        "so the results will not be recorded in Liquibase Hub.\n" +
                        "To register the changelog with your Hub Project run 'liquibase registerchangelog'.\n" +
                        "Learn more at https://hub.liquibase.com.";
        Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
        log.warning(message);
    }

    protected void checkLiquibaseTables(Database database) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setInternal(true);
    }

    private DatabaseChangeLog parseChangeLogFile(String changeLogFile) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }
}
