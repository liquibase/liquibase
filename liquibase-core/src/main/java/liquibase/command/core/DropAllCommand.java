package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.hub.HubConfiguration;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubChangeLog;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @deprecated Implement commands with {@link liquibase.command.CommandStep} and call them with {@link liquibase.command.CommandFactory#getCommandDefinition(String...)}.
 */
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

        this.schemas = finalList.toArray(new CatalogAndSchema[0]);


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
    public CommandResult run() throws Exception {
        final CommandScope commandScope = new CommandScope("dropAllInternal");
        commandScope.addArgumentValue(InternalDropAllCommandStep.CHANGELOG_ARG, this.liquibase.getDatabaseChangeLog());
        commandScope.addArgumentValue(InternalDropAllCommandStep.CHANGELOG_FILE_ARG, this.changeLogFile);
        commandScope.addArgumentValue(InternalDropAllCommandStep.DATABASE_ARG, this.database);
        commandScope.addArgumentValue(InternalDropAllCommandStep.HUB_CONNECTION_ID_ARG, this.hubConnectionId);
        commandScope.addArgumentValue(InternalDropAllCommandStep.SCHEMAS_ARG, this.schemas);

        final CommandResults results = commandScope.execute();

        return new CommandResult("All objects dropped from " + database.getConnection().getConnectionUserName() + "@" + database.getConnection().getURL());
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
