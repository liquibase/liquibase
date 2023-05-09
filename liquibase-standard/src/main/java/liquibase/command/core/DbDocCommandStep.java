package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.*;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.DBDocVisitor;
import liquibase.command.*;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DbDocCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"dbDoc"};
    public static final CommandArgumentDefinition<String> OUTPUT_DIRECTORY_ARG;
    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> CATALOG_AND_SCHEMAS_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        OUTPUT_DIRECTORY_ARG = builder.argument("outputDirectory", String.class)
                .required()
                .description("The directory where the documentation is generated")
                .build();
        SCHEMAS_ARG = builder.argument("schemas", String.class)
                .description("Database schemas to include objects from in reporting")
                .build();
        CATALOG_AND_SCHEMAS_ARG = builder.argument("catalogAndSchemas", CatalogAndSchema[].class)
                .hidden()
                .optional()
                .build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Changeset labels to match")
                .build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
                .description("Changeset contexts to match")
                .build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class, DatabaseChangeLog.class, ChangeLogParameters.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Generates JavaDoc documentation for the existing database and changelogs");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final Contexts contexts = new Contexts(commandScope.getArgumentValue(CONTEXTS_ARG));
        final LabelExpression labelExpression = new LabelExpression(commandScope.getArgumentValue(LABEL_FILTER_ARG));
        final ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);
        final String outputDirectory = commandScope.getArgumentValue(OUTPUT_DIRECTORY_ARG);
        final String schemaList = commandScope.getArgumentValue(SCHEMAS_ARG);
        final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        final CatalogAndSchema[] catalogAndSchemaArg = commandScope.getArgumentValue(CATALOG_AND_SCHEMAS_ARG);
        final CatalogAndSchema[] catalogAndSchemas = getCatalogAndSchema(schemaList, catalogAndSchemaArg, database);

        Scope.getCurrentScope().getLog(getClass()).info("Generating Database Documentation");
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        try {
            final DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
            checkLiquibaseTables(false, databaseChangeLog, new Contexts(), new LabelExpression(), database);

            databaseChangeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator = new ChangeLogIterator(databaseChangeLog, new DbmsChangeSetFilter(database));

            DBDocVisitor visitor = new DBDocVisitor(database);
            logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));

            final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
            Resource resource = pathHandlerFactory.getResource(outputDirectory);
            visitor.writeHTML(resource, resourceAccessor, catalogAndSchemas);
            resultsBuilder.addResult("statusCode", 0);
        } catch (IOException e) {
            resultsBuilder.addResult("statusCode", 1);
            throw new LiquibaseException(e);
        }
    }

    /**
     * Finds the appropriate catalog and schema for the query
     *
     * @param schemas             the comma list of schemas input to the command via the command framework or cli
     * @param catalogAndSchemaArg the list of catalog and schema from Main or Liquibase classes. This will be used if present.
     * @param database            the database to find the catalog and schema against
     * @return the catalog and schema array to use
     */
    private CatalogAndSchema[] getCatalogAndSchema(String schemas, CatalogAndSchema[] catalogAndSchemaArg, Database database) {
        if (catalogAndSchemaArg != null) {
            return catalogAndSchemaArg;
        } else if (schemas != null) {
            List<CatalogAndSchema> schemaList = new ArrayList<>();
            for (String schema : schemas.split(",")) {
                schemaList.add(new CatalogAndSchema(null, schema).customize(database));
            }
            return schemaList.toArray(new CatalogAndSchema[0]);
        }
        return new CatalogAndSchema[]{new CatalogAndSchema(null, null)};
    }

    // Unsure if this method is still needed with the refactoring, but I'm leaving it in now to keep the logic the same.
    public void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                     Contexts contexts, LabelExpression labelExpression, Database database) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }
}
