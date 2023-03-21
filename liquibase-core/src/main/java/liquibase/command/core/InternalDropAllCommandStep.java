package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;

@SuppressWarnings("java:S1144")
public class InternalDropAllCommandStep extends AbstractCommandStep {

    protected static final String[] COMMAND_NAME = {"internalDropAll"};
    private final Logger log = Scope.getCurrentScope().getLog(getClass());

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<DatabaseChangeLog> CHANGELOG_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> SCHEMAS_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        DATABASE_ARG = builder.argument("database", Database.class).required()
                .description("Database to drop objects in").build();
        SCHEMAS_ARG = builder.argument("schemas", CatalogAndSchema[].class)
                .description("Schemas to drop objects in").build();
        CHANGELOG_ARG = builder.argument("changelog", DatabaseChangeLog.class).build();
        CHANGELOG_FILE_ARG = builder.argument("changeFile", String.class)
                .description("The root changelog").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        LockService lockService = LockServiceFactory.getInstance().getLockService(commandScope.getArgumentValue(DATABASE_ARG));

        try {
            lockService.waitForLock();
            try {
                for (CatalogAndSchema schema : commandScope.getArgumentValue(SCHEMAS_ARG)) {
                    log.info("Dropping Database Objects in schema: " + schema);
                    checkLiquibaseTables(commandScope.getArgumentValue(DATABASE_ARG));
                    commandScope.getArgumentValue(DATABASE_ARG).dropDatabaseObjects(schema);
                }
            } catch (LiquibaseException liquibaseException) {
                String message =
                   String.format("Error occurred during dropAll: %s%nIt is possible that not all objects were dropped.%n",
                           liquibaseException.getMessage());

                Scope.getCurrentScope().getUI().sendMessage(message);
                log.severe(message, liquibaseException);
            }
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
}
