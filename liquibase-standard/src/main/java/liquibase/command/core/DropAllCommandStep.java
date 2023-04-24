package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.LockService;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DropAllCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"dropAll"};

    private static final Logger LOG = Scope.getCurrentScope().getLog(DropAllCommandStep.class);

    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> CATALOG_AND_SCHEMAS_ARG;
    public static final CommandArgumentDefinition<UUID> HUB_CONNECTION_ID_ARG;
    public static final CommandArgumentDefinition<UUID> HUB_PROJECT_ID_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        SCHEMAS_ARG = builder.argument("schemas", String.class).description("Schemas to include in drop").build();
        CATALOG_AND_SCHEMAS_ARG = builder.argument("catalogAndSchemas", CatalogAndSchema[].class)
                .description("Catalog and schemas to include in drop. It has precedence over SCHEMAS_ARG").supersededBy(SCHEMAS_ARG).hidden().build();
        SCHEMAS_ARG.setSupersededBy(CATALOG_AND_SCHEMAS_ARG);
        HUB_CONNECTION_ID_ARG = builder.argument("hubConnectionId", UUID.class)
                .description("Used to identify the specific Connection in which to record or extract data at Liquibase Hub. Available in your Liquibase Hub Project at https://hub.liquibase.com.").build();
        HUB_PROJECT_ID_ARG = builder.argument("hubProjectId", UUID.class)
                .description("Used to identify the specific Project in which to record at Liquibase Hub. Available in your Liquibase Hub account at https://hub.liquibase.com.").build();
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
        return Arrays.asList(Database.class, LockService.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);

        List<CatalogAndSchema> catalogAndSchemas = getCatalogAndSchemas(database, commandScope);

        try {
            for (CatalogAndSchema catalogAndSchema : catalogAndSchemas) {
                LOG.info("Dropping Database Objects in schema: " + catalogAndSchema);
                database.dropDatabaseObjects(catalogAndSchema);
            }
        } catch (LiquibaseException liquibaseException) {
            String message =
                    String.format("Error occurred during dropAll: %s%nIt is possible that not all objects were dropped.%n",
                            liquibaseException.getMessage());

            Scope.getCurrentScope().getUI().sendMessage(message);
            LOG.severe(message, liquibaseException);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        Scope.getCurrentScope().getUI().sendMessage("All objects dropped from " + database.getConnection().getConnectionUserName() + "@" + database.getConnection().getURL());
        resultsBuilder.addResult("statusCode", 0);
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

}
