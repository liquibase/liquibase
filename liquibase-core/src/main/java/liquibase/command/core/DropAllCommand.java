package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DropAllCommand extends AbstractCommand<CommandResult> {

    private Database database;
    private CatalogAndSchema[] schemas;
    private String changeLogFile;
    private UUID hubConnectionId;
    private Liquibase liquibase;

    @Override
    public String getName() {
        return "dropAll";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public void setLiquibase(Liquibase liquibase) {
        this.liquibase = liquibase;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public CatalogAndSchema[] getSchemas() {
        return schemas;
    }

    public void setSchemas(CatalogAndSchema[] schemas) {
        this.schemas = schemas;
    }

    public DropAllCommand setSchemas(String... schemas) {
        if ((schemas == null) || (schemas.length == 0) || (schemas[0] == null)) {
            this.schemas = null;
            return this;
        }

        schemas = StringUtil.join(schemas, ",").split("\\s*,\\s*");
        List<CatalogAndSchema> finalList = new ArrayList<>();
        for (String schema : schemas) {
            finalList.add(new CatalogAndSchema(null, schema).customize(database));
        }

        this.schemas = finalList.toArray(new CatalogAndSchema[finalList.size()]);


        return this;

    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public void setHubConnectionId(String hubConnectionIdString) {
        if (hubConnectionIdString == null) {
            return;
        }
        this.hubConnectionId = UUID.fromString(hubConnectionIdString);
    }

    @Override
    protected CommandResult run() throws Exception {
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        Logger log = Scope.getCurrentScope().getLog(getClass());
        HubUpdater hubUpdater = null;
        try {
            lockService.waitForLock();

            boolean doSyncHub = true;
            DatabaseChangeLog changeLog = null;
            if (StringUtil.isNotEmpty(changeLogFile)) {
                //
                // Let the user know they can register for Hub
                //
                changeLog = liquibase.getDatabaseChangeLog();
                hubUpdater = new HubUpdater(new Date(), changeLog, database);
                hubUpdater.register(changeLogFile);
                doSyncHub = checkForRegisteredChangeLog(changeLog);
            }

            for (CatalogAndSchema schema : schemas) {
                log.info("Dropping Database Objects in schema: " + schema);
                checkLiquibaseTables(false, null, new Contexts(), new LabelExpression());
                database.dropDatabaseObjects(schema);
            }
            if (hubUpdater != null && (doSyncHub || hubConnectionId != null)) {
                hubUpdater.syncHub(changeLogFile, database, changeLog, hubConnectionId);
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

        return new CommandResult("All objects dropped from " + database.getConnection().getConnectionUserName() + "@" + database.getConnection().getURL());
    }

    private boolean checkForRegisteredChangeLog(DatabaseChangeLog changeLog) throws LiquibaseHubException {
        Logger log = Scope.getCurrentScope().getLog(getClass());
        HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        String apiKey = StringUtil.trimToNull(hubConfiguration.getLiquibaseHubApiKey());
        String hubMode = StringUtil.trimToNull(hubConfiguration.getLiquibaseHubMode());
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

    protected void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
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
