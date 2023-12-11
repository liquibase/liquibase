package liquibase.command.core.helpers;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.core.DiffCommandStep;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.List;

/**
 * Prepares objects used by database comparison Commands like diff, diffChangelog, etc
 */
public class PreCompareCommandStep extends AbstractHelperCommandStep {

    protected static final String[] COMMAND_NAME = {"preCompareCommandStep"};
    public static final CommandArgumentDefinition<String> EXCLUDE_OBJECTS_ARG;
    public static final CommandArgumentDefinition<String> INCLUDE_OBJECTS_ARG;
    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> DIFF_TYPES_ARG;

    public static final CommandArgumentDefinition<String> REFERENCE_SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> OUTPUT_SCHEMAS_ARG;

    public static final CommandArgumentDefinition<CompareControl> COMPARE_CONTROL_ARG;
    public static final CommandArgumentDefinition<Class[]> SNAPSHOT_TYPES_ARG;
    public static final CommandArgumentDefinition<ObjectChangeFilter> OBJECT_CHANGE_FILTER_ARG;

    public static final CommandResultDefinition<CompareControl> COMPARE_CONTROL_RESULT;
    public static final CommandResultDefinition<Class[]> SNAPSHOT_TYPES_RESULT;
    public static final CommandResultDefinition<ObjectChangeFilter> OBJECT_CHANGE_FILTER_RESULT;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        EXCLUDE_OBJECTS_ARG = builder.argument("excludeObjects", String.class)
                .description("Objects to exclude from diff").build();
        INCLUDE_OBJECTS_ARG = builder.argument("includeObjects", String.class)
                .description("Objects to include in diff").build();
        SCHEMAS_ARG = builder.argument("schemas", String.class)
                .description("Schemas to include in diff").build();
        DIFF_TYPES_ARG = builder.argument("diffTypes", String.class)
                .description("Types of objects to compare").build();

        REFERENCE_SCHEMAS_ARG = builder.argument("referenceSchemas", String.class)
                .description("Schemas names on reference database to use in diff. This is a CSV list.").build();
        OUTPUT_SCHEMAS_ARG = builder.argument("outputSchemas", String.class)
                .description("Output schemas names. This is a CSV list.").build();

        COMPARE_CONTROL_ARG = builder.argument("compareControl", CompareControl.class).hidden().build();
        SNAPSHOT_TYPES_ARG = builder.argument("snapshotTypes", Class[].class).hidden().build();
        OBJECT_CHANGE_FILTER_ARG = builder.argument("objectChangeFilter", ObjectChangeFilter.class).hidden().build();

        COMPARE_CONTROL_RESULT = builder.result("compareControl", CompareControl.class).build();
        SNAPSHOT_TYPES_RESULT = builder.result("snapshotTypes", Class[].class).build();
        OBJECT_CHANGE_FILTER_RESULT = builder.result("objectChangeFilter", ObjectChangeFilter.class).build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class);
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Arrays.asList(CompareControl.class, ObjectChangeFilter.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database targetDatabase = getTargetDatabase(commandScope);
        ObjectChangeFilter objectChangeFilter = this.getObjectChangeFilter(commandScope);
        String diffTypes = commandScope.getArgumentValue(DIFF_TYPES_ARG);
        CompareControl compareControl = this.getCompareControl(commandScope, targetDatabase, diffTypes);
        Scope.getCurrentScope().addMdcValue(MdcKey.DIFF_TYPES, diffTypes);
        Class<? extends DatabaseObject>[] snapshotTypes = getSnapshotTypes(commandScope, diffTypes);

        resultsBuilder.addResult(COMPARE_CONTROL_RESULT, compareControl)
                      .addResult(OBJECT_CHANGE_FILTER_RESULT, objectChangeFilter)
                      .addResult(SNAPSHOT_TYPES_RESULT, snapshotTypes);
    }

    private static Database getTargetDatabase(CommandScope commandScope) {
        Object database = commandScope.getDependency(Database.class);
        if (database == null) {
            database = commandScope.getDependency(ReferenceDatabase.class);
        }
        return (Database) database;
    }

    private static Class<? extends DatabaseObject>[] getSnapshotTypes(CommandScope commandScope, String diffTypes) {
        if (commandScope.getArgumentValue(SNAPSHOT_TYPES_ARG) != null) {
            return commandScope.getArgumentValue(SNAPSHOT_TYPES_ARG);
        }
        return DiffCommandStep.parseSnapshotTypes(diffTypes);
    }

    private ObjectChangeFilter getObjectChangeFilter(CommandScope commandScope) {
        if (commandScope.getArgumentValue(OBJECT_CHANGE_FILTER_ARG) != null) {
            return commandScope.getArgumentValue(OBJECT_CHANGE_FILTER_ARG);
        }
        String excludeObjects = commandScope.getArgumentValue(EXCLUDE_OBJECTS_ARG);
        String includeObjects = commandScope.getArgumentValue(INCLUDE_OBJECTS_ARG);

        if ((excludeObjects != null) && (includeObjects != null)) {
            throw new UnexpectedLiquibaseException(
                    String.format(coreBundle.getString("cannot.specify.both"),
                            EXCLUDE_OBJECTS_ARG.getName(), INCLUDE_OBJECTS_ARG.getName()));
        }

        Scope.getCurrentScope().addMdcValue(MdcKey.EXCLUDE_OBJECTS, excludeObjects);
        Scope.getCurrentScope().addMdcValue(MdcKey.INCLUDE_OBJECTS, includeObjects);

        ObjectChangeFilter objectChangeFilter = null;
        if (excludeObjects != null) {
            objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE,
                    excludeObjects);
        }
        if (includeObjects != null) {
            objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE,
                    includeObjects);
        }

        return objectChangeFilter;
    }

    private CompareControl getCompareControl(CommandScope commandScope, Database database, String diffTypes) {
        if (commandScope.getArgumentValue(COMPARE_CONTROL_ARG) != null) {
            return commandScope.getArgumentValue(COMPARE_CONTROL_ARG);
        }
        String schemas = commandScope.getArgumentValue(SCHEMAS_ARG);
        String outputSchemas = commandScope.getArgumentValue(OUTPUT_SCHEMAS_ARG);
        String referenceSchemas = commandScope.getArgumentValue(REFERENCE_SCHEMAS_ARG);
        logMdcProperties(schemas, outputSchemas, referenceSchemas);
        CompareControl.SchemaComparison[] finalSchemaComparisons = CompareControl.computeSchemas(
                schemas,
                referenceSchemas,
                outputSchemas,
                commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_CATALOG_NAME_ARG),
                commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG),
                commandScope.getArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DEFAULT_CATALOG_NAME_ARG),
                commandScope.getArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DEFAULT_SCHEMA_NAME_ARG),
                database).finalSchemaComparisons;

        return new CompareControl(finalSchemaComparisons, diffTypes);
    }

    public static void logMdcProperties(String schemas, String outputSchemas, String referenceSchemas) {
        Scope.getCurrentScope().addMdcValue(MdcKey.SCHEMAS, schemas);
        Scope.getCurrentScope().addMdcValue(MdcKey.OUTPUT_SCHEMAS, outputSchemas);
        Scope.getCurrentScope().addMdcValue(MdcKey.REFERENCE_SCHEMAS, referenceSchemas);
    }
}
