package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.command.*;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.snapshot.SnapshotControl;
import liquibase.util.StringUtil;
import liquibase.util.ValueHandlerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DropAllCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"dropAll"};

    private final Logger log = Scope.getCurrentScope().getLog(DropAllCommandStep.class);

    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> CATALOG_AND_SCHEMAS_ARG;

    public static final CommandArgumentDefinition<Boolean> REQUIRE_FORCE_ARG;

    public static final CommandArgumentDefinition<Boolean> FORCE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        SCHEMAS_ARG = builder.argument("schemas", String.class).description("Schemas to include in drop").build();
        CATALOG_AND_SCHEMAS_ARG = builder.argument("catalogAndSchemas", CatalogAndSchema[].class)
                .description("Catalog and schemas to include in drop. It has precedence over SCHEMAS_ARG").supersededBy(SCHEMAS_ARG).hidden().build();
        SCHEMAS_ARG.setSupersededBy(CATALOG_AND_SCHEMAS_ARG);
        REQUIRE_FORCE_ARG = builder.argument("requireForce", Boolean.class)
                .description("Argument to require user of dropAll to supply a 'force' argument, with values of 'true' or 'false'. The default is 'false'.")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .required()
                .defaultValue(false)
                .build();
        FORCE_ARG = builder.argument("force", Boolean.class)
                .description("Argument to allow use of dropAll with values of 'true' or 'false'. The default is 'false'.")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .required()
                .defaultValue(false)
                .build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Drop all database objects owned by the user");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);
        if (Boolean.FALSE.equals(commandScope.getArgumentValue(REQUIRE_FORCE_ARG))) {
            String noRequirementForForceMessage =
                    String.format("The drop-all command may result in unrecoverable destructive changes to objects at '%s'.%n" +
                                    "To protect against unwanted drops, set --requireForce=true, which " +
                                    "will require a --force=true flag on the command.%nLearn more at https://docs.liquibase.com/dropall.%n",
                            database.getConnection().getURL());
            Scope.getCurrentScope().getUI().sendMessage("INFO: " + noRequirementForForceMessage);
        } else {
            boolean force = commandScope.getArgumentValue(FORCE_ARG);
            if (!force) {
                String message =
                    "The drop-all command may result in unrecoverable destructive changes by dropping all the objects at database '" +
                    database.getConnection().getURL() +
                    "'.  This message can be suppressed and the drop-all command executed by adding the --force flag.";
                throw new LiquibaseException(message);
            }
        }
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        List<CatalogAndSchema> catalogAndSchemas = getCatalogAndSchemas(database, commandScope);

        try {
            for (CatalogAndSchema catalogAndSchema : catalogAndSchemas) {
                log.info("Dropping Database Objects in schema: " + catalogAndSchema);
                SnapshotControl snapshotControl = getSnapshotControl(commandScope, database);
                if (snapshotControl != null) {
                    database.dropDatabaseObjects(catalogAndSchema, snapshotControl);
                } else {
                    database.dropDatabaseObjects(catalogAndSchema);
                }
            }
        } catch (LiquibaseException liquibaseException) {
            String message =
                    String.format("Error occurred during dropAll: %s%nIt is possible that not all objects were dropped.%n",
                            liquibaseException.getMessage());

            Scope.getCurrentScope().getUI().sendMessage(message);
            log.severe(message, liquibaseException);
            throw liquibaseException;
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            lockService.releaseLock();
            lockService.destroy();
            resetServices();
        }

        Scope.getCurrentScope().getUI().sendMessage("All objects dropped from " + database.getConnection().getConnectionUserName() + "@" + database.getConnection().getURL());
        resultsBuilder.addResult("statusCode", 0);
    }

    public SnapshotControl getSnapshotControl(CommandScope commandScope, Database database) {
        // This is purposefully returning null. It is overridden in other implementations of this command step, like in pro.
        return null;
    }

    private List<CatalogAndSchema> getCatalogAndSchemas(Database database, CommandScope commandScope) {
        String schemas = commandScope.getArgumentValue(SCHEMAS_ARG);
        CatalogAndSchema[] catalogAndSchemas = commandScope.getArgumentValue(CATALOG_AND_SCHEMAS_ARG);

        if (catalogAndSchemas != null && catalogAndSchemas.length > 0) {
            return Arrays.asList(catalogAndSchemas);
        }

        List<CatalogAndSchema> computedCatalogAndSchemas = new ArrayList<>();
        if (schemas == null || schemas.isEmpty()) {
            computedCatalogAndSchemas.add(new CatalogAndSchema(database.getDefaultCatalogName(), database.getDefaultSchemaName()));
        } else {
            StringUtil.splitAndTrim(schemas, ",").forEach(s ->
                    computedCatalogAndSchemas.add(new CatalogAndSchema(null, s).customize(database))
            );
        } return computedCatalogAndSchemas;
    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

}
