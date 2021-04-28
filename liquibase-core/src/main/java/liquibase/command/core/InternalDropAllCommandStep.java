package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.hub.HubConfiguration;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.HubUpdater;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubChangeLog;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.Date;
import java.util.UUID;

public class InternalDropAllCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"internalDropAll"};

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<DatabaseChangeLog> CHANGELOG_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<UUID> HUB_CONNECTION_ID_ARG;

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
            .description("The Hub connection ID").build();
    }

    @Override
    public String[] getName() {
        return COMMAND_NAME;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        LockService lockService = LockServiceFactory.getInstance().getLockService(commandScope.getArgumentValue(DATABASE_ARG));
        Logger log = Scope.getCurrentScope().getLog(getClass());
        HubUpdater hubUpdater = null;
        try {
            lockService.waitForLock();

            boolean doSyncHub = true;
            DatabaseChangeLog changeLog = null;
            HubChangeLog hubChangeLog = null;
            if (StringUtil.isNotEmpty(commandScope.getArgumentValue(CHANGELOG_FILE_ARG))) {
                //
                // Let the user know they can register for Hub
                //
                changeLog = parseChangeLogFile(commandScope.getArgumentValue(CHANGELOG_FILE_ARG));
                hubUpdater = new HubUpdater(new Date(), changeLog, commandScope.getArgumentValue(DATABASE_ARG));
                hubUpdater.register(commandScope.getArgumentValue(CHANGELOG_FILE_ARG));

                //
                // Access the HubChangeLog and check to see
                // if we should run syncHub
                //
                hubChangeLog = getHubChangeLog(changeLog);
                doSyncHub = checkForRegisteredChangeLog(changeLog, hubChangeLog);
            }

            for (CatalogAndSchema schema : commandScope.getArgumentValue(SCHEMAS_ARG)) {
                log.info("Dropping Database Objects in schema: " + schema);
                checkLiquibaseTables(false, null, new Contexts(), new LabelExpression(), commandScope.getArgumentValue(DATABASE_ARG));
                commandScope.getArgumentValue(DATABASE_ARG).dropDatabaseObjects(schema);
            }

            //
            // Run syncHub if we are connected to Hub and either
            // the changelog is registered, or we have a Hub connection ID
            //
            if (hubUpdater != null && (doSyncHub || commandScope.getArgumentValue(HUB_CONNECTION_ID_ARG) != null)) {
                hubUpdater.syncHub(commandScope.getArgumentValue(CHANGELOG_FILE_ARG), changeLog, commandScope.getArgumentValue(HUB_CONNECTION_ID_ARG));
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
        resultsBuilder.addResult("statusMessage", "Successfully executed dropAll");
    }

    //
    // Return a HubChangeLog object if available
    // If not available then return null
    // If the HubChangeLog has been deleted then throw
    // a LiquibaseHubException
    //
    private HubChangeLog getHubChangeLog(DatabaseChangeLog changeLog) throws LiquibaseHubException {
        String apiKey = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue());
        String hubMode = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_MODE.getCurrentValue());
        String changeLogId = changeLog.getChangeLogId();
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (apiKey == null || hubMode.equals("off") || !hubServiceFactory.isOnline()) {
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
                "The operation did not complete and will not be reported to Hub because the\n" +  "" +
                "registered changelog has been deleted by someone in your organization.\n" +
                "Learn more at http://hub.liquibase.com";
            throw new LiquibaseHubException(message);
        }
        return hubChangeLog;
    }

    private boolean checkForRegisteredChangeLog(DatabaseChangeLog changeLog, HubChangeLog hubChangeLog) {
        Logger log = Scope.getCurrentScope().getLog(getClass());
        String changeLogId = changeLog.getChangeLogId();
        if (changeLogId != null && hubChangeLog != null) {
            return true;
        }
        String message =
            "The changelog file specified is not registered with any Liquibase Hub project,\n" +
            "so the results will not be recorded in Liquibase Hub.\n" +
            "To register the changelog with your Hub Project run 'liquibase registerchangelog'.\n" +
            "Learn more at https://hub.liquibase.com.";
        Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
        log.warning(message);
        return false;
    }

    protected void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression, Database database) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Drop all database objects owned by the user");
        commandDefinition.setLongDescription("Drop all database objects owned by the user");
    }

    private DatabaseChangeLog parseChangeLogFile(String changeLogFile) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }
}
