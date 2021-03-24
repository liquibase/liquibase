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

    @Override
    public void run(CommandScope commandScope) throws Exception {
        LockService lockService = LockServiceFactory.getInstance().getLockService(DATABASE_ARG.getValue(commandScope));
        Logger log = Scope.getCurrentScope().getLog(getClass());
        HubUpdater hubUpdater = null;
        try {
            lockService.waitForLock();

            boolean doSyncHub = true;
            DatabaseChangeLog changeLog = null;
            if (StringUtil.isNotEmpty(CHANGELOG_FILE_ARG.getValue(commandScope))) {
                //
                // Let the user know they can register for Hub
                //
                hubUpdater = new HubUpdater(new Date(), changeLog, DATABASE_ARG.getValue(commandScope));
                hubUpdater.register(CHANGELOG_FILE_ARG.getValue(commandScope));
                doSyncHub = checkForRegisteredChangeLog(changeLog);
            }

            for (CatalogAndSchema schema : SCHEMAS_ARG.getValue(commandScope)) {
                log.info("Dropping Database Objects in schema: " + schema);
                checkLiquibaseTables(false, null, new Contexts(), new LabelExpression(), DATABASE_ARG.getValue(commandScope));
                DATABASE_ARG.getValue(commandScope).dropDatabaseObjects(schema);
            }
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
        commandScope.addResult("statusCode", 0);
    }

    private boolean checkForRegisteredChangeLog(DatabaseChangeLog changeLog) throws LiquibaseHubException {
        Logger log = Scope.getCurrentScope().getLog(getClass());
        String apiKey = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue());
        String hubMode = StringUtil.trimToNull(HubConfiguration.LIQUIBASE_HUB_MODE.getCurrentValue());
        String changeLogId = changeLog.getChangeLogId();
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (apiKey == null || hubMode.equals("off") || ! hubServiceFactory.isOnline()) {
            return false;
        }
        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        HubChangeLog hubChangeLog = (changeLogId != null ? service.getHubChangeLog(UUID.fromString(changeLogId)) : null);
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
