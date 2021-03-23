package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
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
import liquibase.util.StringUtil;

import java.util.Date;
import java.util.UUID;

public class DropAllCommand extends AbstractCommand {

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<DatabaseChangeLog> CHANGELOG_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<UUID> HUB_CONNECTION_ID;

    static {
        CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder(DropAllCommand.class);

        DATABASE_ARG = builder.define("database", Database.class)
                .required().build();
        SCHEMAS_ARG = builder.define("schemas", CatalogAndSchema[].class).build();
        CHANGELOG_ARG = builder.define("changelog", DatabaseChangeLog.class).build();
        CHANGELOG_FILE_ARG = builder.define("changelogFile", String.class).build();
        HUB_CONNECTION_ID = builder.define("hubConnectionId", UUID.class).build();
    }

    @Override
    public String[] getName() {
        return new String[]{"dropAll"};
    }


//    public DropAllCommand setSchemas(String... schemas) {
//        if ((schemas == null) || (schemas.length == 0) || (schemas[0] == null)) {
//            this.schemas = null;
//            return this;
//        }
//
//        schemas = StringUtil.join(schemas, ",").split("\\s*,\\s*");
//        List<CatalogAndSchema> finalList = new ArrayList<>();
//        for (String schema : schemas) {
//            finalList.add(new CatalogAndSchema(null, schema).customize(database));
//        }
//
//        this.schemas = finalList.toArray(new CatalogAndSchema[finalList.size()]);
//
//
//        return this;
//
//    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        LockService lockService = LockServiceFactory.getInstance().getLockService(DATABASE_ARG.getValue(commandScope));
        Logger log = Scope.getCurrentScope().getLog(getClass());
        HubUpdater hubUpdater = null;
        try {
            lockService.waitForLock();

            boolean doSyncHub = true;
            DatabaseChangeLog changeLog = null;
            HubChangeLog hubChangeLog = null;
            if (StringUtil.isNotEmpty(CHANGELOG_FILE_ARG.getValue(commandScope))) {
                //
                // Let the user know they can register for Hub
                //
                hubUpdater = new HubUpdater(new Date(), changeLog, DATABASE_ARG.getValue(commandScope));
                hubUpdater.register(CHANGELOG_FILE_ARG.getValue(commandScope));

                //
                // Access the HubChangeLog and check to see
                // if we should run syncHub
                //
                hubChangeLog = getHubChangeLog(changeLog);
                doSyncHub = checkForRegisteredChangeLog(changeLog, hubChangeLog);
            }

            for (CatalogAndSchema schema : SCHEMAS_ARG.getValue(commandScope)) {
                log.info("Dropping Database Objects in schema: " + schema);
                checkLiquibaseTables(false, null, new Contexts(), new LabelExpression(), DATABASE_ARG.getValue(commandScope));
                DATABASE_ARG.getValue(commandScope).dropDatabaseObjects(schema);
            }

            //
            // Tell the user if the HubChangeLog is deactivated
            //
            if (hubUpdater != null && (doSyncHub || HUB_CONNECTION_ID.getValue(commandScope) != null)) {
                hubUpdater.syncHub(CHANGELOG_FILE_ARG.getValue(commandScope), changeLog, HUB_CONNECTION_ID.getValue(commandScope));
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

        Scope.getCurrentScope().getUI().sendMessage("All objects dropped from " + DATABASE_ARG.getValue(commandScope).getConnection().getConnectionUserName() + "@" + DATABASE_ARG.getValue(commandScope).getConnection().getURL());
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

}
