package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DropAllCommand extends AbstractCommand<CommandResult> {

    private Database database;
    private CatalogAndSchema[] schemas;

    private Logger log = LogService.getLog(getClass());

    @Override
    public String getName() {
        return "dropAll";
    }

    @Override
    public CommandValidationErrors validate() {
        CommandValidationErrors commandValidationErrors = new CommandValidationErrors(this);
        return commandValidationErrors;
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

    @Override
    protected CommandResult run() throws Exception {
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        try {
            lockService.waitForLock();

            for (CatalogAndSchema schema : schemas) {
                log.info(LogType.LOG, "Dropping Database Objects in schema: " + schema);
                checkLiquibaseTables(false, null, new Contexts(), new LabelExpression());
                database.dropDatabaseObjects(schema);
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
        ExecutorService.getInstance().reset();
    }

}
