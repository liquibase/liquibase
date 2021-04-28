package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.report.DiffToReport;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.util.StringUtil;

import java.io.PrintStream;
import java.util.Set;

public class InternalDiffCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"internalDiff"};

    public static final CommandArgumentDefinition<Database> REFERENCE_DATABASE_ARG;
    public static final CommandArgumentDefinition<Database> TARGET_DATABASE_ARG;
    public static final CommandArgumentDefinition<Class[]> SNAPSHOT_TYPES_ARG;
    public static final CommandArgumentDefinition<SnapshotListener> SNAPSHOT_LISTENER_ARG;
    public static final CommandArgumentDefinition<SnapshotControl> REFERENCE_SNAPSHOT_CONTROL_ARG;
    public static final CommandArgumentDefinition<SnapshotControl> TARGET_SNAPSHOT_CONTROL_ARG;
    public static final CommandArgumentDefinition<ObjectChangeFilter> OBJECT_CHANGE_FILTER_ARG;
    public static final CommandArgumentDefinition<CompareControl> COMPARE_CONTROL_ARG;
    public static final CommandArgumentDefinition<Boolean> PRINT_RESULT;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        REFERENCE_DATABASE_ARG = builder.argument("referenceDatabase", Database.class).required().build();
        TARGET_DATABASE_ARG = builder.argument("targetDatabase", Database.class).required().build();
        SNAPSHOT_TYPES_ARG = builder.argument("snapshotTypes", Class[].class).required().build();
        SNAPSHOT_LISTENER_ARG = builder.argument("snapshotListener", SnapshotListener.class).build();
        REFERENCE_SNAPSHOT_CONTROL_ARG = builder.argument("referenceSnapshotControl", SnapshotControl.class).build();
        TARGET_SNAPSHOT_CONTROL_ARG = builder.argument("targetSnapshotControl", SnapshotControl.class).build();
        OBJECT_CHANGE_FILTER_ARG = builder.argument("objectChangeFilter", ObjectChangeFilter.class).build();
        COMPARE_CONTROL_ARG = builder.argument("compareControl", CompareControl.class).required().build();
        PRINT_RESULT = builder.argument("printResult", Boolean.class).defaultValue(true).build();
    }


    @Override
    public String[] getName() {
        return COMMAND_NAME;
    }

    public static Class<? extends DatabaseObject>[] parseSnapshotTypes(String... snapshotTypes) {
        if ((snapshotTypes == null) || (snapshotTypes.length == 0) || (snapshotTypes[0] == null)) {
            return new Class[0];
        }

        Set<Class<? extends DatabaseObject>> types = DatabaseObjectFactory.getInstance().parseTypes(StringUtil.join(snapshotTypes, ","));

        Class<? extends DatabaseObject>[] returnTypes = new Class[types.size()];
        int i = 0;
        for (Class<? extends DatabaseObject> type : types) {
            returnTypes[i++] = type;
        }

        return returnTypes;
    }


    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        InternalSnapshotCommandStep.logUnsupportedDatabase(commandScope.getArgumentValue(REFERENCE_DATABASE_ARG), this.getClass());

        DiffResult diffResult = createDiffResult(commandScope);

        resultsBuilder.addResult("diffResult", diffResult);
        Boolean printResult = commandScope.getArgumentValue(PRINT_RESULT);
        if (printResult == null || ! printResult) {
            return;
        }

        final PrintStream printStream = new PrintStream(resultsBuilder.getOutputStream());
        new DiffToReport(diffResult, printStream).print();
        printStream.flush();

        resultsBuilder.addResult("statusCode", 0);
        resultsBuilder.addResult("statusMessage", "Successfully executed diff");
    }

    public DiffResult createDiffResult(CommandScope commandScope) throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot referenceSnapshot = createReferenceSnapshot(commandScope);
        DatabaseSnapshot targetSnapshot = createTargetSnapshot(commandScope);

        final CompareControl compareControl = commandScope.getArgumentValue(COMPARE_CONTROL_ARG);
        referenceSnapshot.setSchemaComparisons(compareControl.getSchemaComparisons());
        if (targetSnapshot != null) {
            targetSnapshot.setSchemaComparisons(compareControl.getSchemaComparisons());
        }

        return DiffGeneratorFactory.getInstance().compare(referenceSnapshot, targetSnapshot, compareControl);
    }

    protected DatabaseSnapshot createTargetSnapshot(CommandScope commandScope) throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas;
        CompareControl compareControl = commandScope.getArgumentValue(COMPARE_CONTROL_ARG);
        Database targetDatabase = commandScope.getArgumentValue(TARGET_DATABASE_ARG);
        SnapshotControl snapshotControl = commandScope.getArgumentValue(TARGET_SNAPSHOT_CONTROL_ARG);
        Class<? extends DatabaseObject>[] snapshotTypes = commandScope.getArgumentValue(SNAPSHOT_TYPES_ARG);
        SnapshotListener snapshotListener = commandScope.getArgumentValue(SNAPSHOT_LISTENER_ARG);

        if ((compareControl == null) || (compareControl.getSchemaComparisons() == null)) {
            schemas = new CatalogAndSchema[]{targetDatabase.getDefaultSchema()};
        } else {
            schemas = new CatalogAndSchema[compareControl.getSchemaComparisons().length];

            int i = 0;
            for (CompareControl.SchemaComparison comparison : compareControl.getSchemaComparisons()) {
                CatalogAndSchema schema;
                if (targetDatabase.supportsSchemas()) {
                    schema = new CatalogAndSchema(targetDatabase.getDefaultCatalogName(), comparison.getComparisonSchema().getSchemaName());
                } else {
                    schema = new CatalogAndSchema(comparison.getComparisonSchema().getSchemaName(), comparison.getComparisonSchema().getSchemaName());
                }

                schemas[i++] = schema;
            }
        }

        if (snapshotControl == null) {
            snapshotControl = new SnapshotControl(targetDatabase, snapshotTypes);
        }
        if (snapshotListener != null) {
            snapshotControl.setSnapshotListener(snapshotListener);
        }
        ObjectQuotingStrategy originalStrategy = targetDatabase.getObjectQuotingStrategy();
        try {
            targetDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            return SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, targetDatabase, snapshotControl);
        } finally {
            targetDatabase.setObjectQuotingStrategy(originalStrategy);
        }
    }

    protected DatabaseSnapshot createReferenceSnapshot(CommandScope commandScope) throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas;
        CompareControl compareControl = commandScope.getArgumentValue(COMPARE_CONTROL_ARG);
        Database targetDatabase = commandScope.getArgumentValue(TARGET_DATABASE_ARG);
        Database referenceDatabase = commandScope.getArgumentValue(REFERENCE_DATABASE_ARG);
        SnapshotControl snapshotControl = commandScope.getArgumentValue(REFERENCE_SNAPSHOT_CONTROL_ARG);
        Class<? extends DatabaseObject>[] snapshotTypes = commandScope.getArgumentValue(SNAPSHOT_TYPES_ARG);
        ObjectChangeFilter objectChangeFilter = commandScope.getArgumentValue(OBJECT_CHANGE_FILTER_ARG);
        SnapshotListener snapshotListener = commandScope.getArgumentValue(SNAPSHOT_LISTENER_ARG);

        if ((compareControl == null) || (compareControl.getSchemaComparisons() == null)) {
            schemas = new CatalogAndSchema[]{targetDatabase.getDefaultSchema()};
        } else {
            schemas = new CatalogAndSchema[compareControl.getSchemaComparisons().length];

            int i = 0;
            for (CompareControl.SchemaComparison comparison : compareControl.getSchemaComparisons()) {
                CatalogAndSchema schema;
                if (referenceDatabase.supportsSchemas()) {
                    schema = new CatalogAndSchema(referenceDatabase.getDefaultCatalogName(), comparison.getReferenceSchema().getSchemaName());
                } else {
                    schema = new CatalogAndSchema(comparison.getReferenceSchema().getSchemaName(), comparison.getReferenceSchema().getSchemaName());
                }
                schemas[i++] = schema;
            }
        }

        if (snapshotControl == null) {
            snapshotControl = new SnapshotControl(referenceDatabase, objectChangeFilter, snapshotTypes);
        }
        if (snapshotListener != null) {
            snapshotControl.setSnapshotListener(snapshotListener);
        }

        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
            referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            return SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, referenceDatabase, snapshotControl);
        } finally {
            referenceDatabase.setObjectQuotingStrategy(originalStrategy);
        }
    }

}

