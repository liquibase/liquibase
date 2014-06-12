package liquibase.command;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

public class DropAllCommand extends AbstractCommand {

    private Database database;
    private CatalogAndSchema[] schemas;

    @Override
    public String getName() {
        return "dropAll";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }


    public Database getDatabase() {
        return database;
    }

    public DropAllCommand setDatabase(Database database) {
        this.database = database;
        return this;
    }

    public CatalogAndSchema[] getSchemas() {
        return schemas;
    }

    public DropAllCommand setSchemas(CatalogAndSchema[] schemas) {
        this.schemas = schemas;
        return this;
    }

    @Override
    protected Object run() throws Exception {
        Logger log = LogFactory.getInstance().getLog();

        CatalogAndSchema[] schemas = this.schemas;
        if (schemas == null) {
            schemas = new CatalogAndSchema[] {
                    new CatalogAndSchema(getDatabase().getDefaultCatalogName(), getDatabase().getDefaultSchemaName())
            };
        }

        try {
            ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(getDatabase());
            changeLogHistoryService.init();
            LockServiceFactory.getInstance().getLockService(getDatabase()).init();

            LockServiceFactory.getInstance().getLockService(database).waitForLock();

            for (CatalogAndSchema schema : schemas) {
                log.info("Dropping Database Objects in schema: " + schema);
                getDatabase().dropDatabaseObjects(schema);
            }
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            try {
                LockServiceFactory.getInstance().getLockService(database).releaseLock();
                LockServiceFactory.getInstance().getLockService(database).reset();
            } catch (LockException e) {
                log.severe("Unable to release lock: " + e.getMessage());
            }
        }

        return null;
    }
}
