package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandScope;
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

public class DiffCommand extends AbstractCommand {

    public static final CommandArgumentDefinition<Database> REFERENCE_DATABASE_ARG;
    public static final CommandArgumentDefinition<Database> TARGET_DATABASE_ARG;
    public static final CommandArgumentDefinition<Class[]> SNAPSHOT_TYPES_ARG;
    public static final CommandArgumentDefinition<SnapshotListener> SNAPSHOT_LISTENER_ARG;
    public static final CommandArgumentDefinition<SnapshotControl> REFERENCE_SNAPSHOT_CONTROL_ARG;
    public static final CommandArgumentDefinition<SnapshotControl> TARGET_SNAPSHOT_CONTROL_ARG;
    public static final CommandArgumentDefinition<ObjectChangeFilter> OBJECT_CHANGE_FILTER_ARG;
    public static final CommandArgumentDefinition<CompareControl> COMPARE_CONTROL_ARG;
    public static final CommandArgumentDefinition<PrintStream> OUTPUT_STREAM_ARG;

    static {
        final CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder(DiffCommand.class);
        REFERENCE_DATABASE_ARG = builder.define("referenceDatabase", Database.class).required().build();
        TARGET_DATABASE_ARG = builder.define("targetDatabase", Database.class).required().build();
        SNAPSHOT_TYPES_ARG = builder.define("snapshotTypes", Class[].class).required().build();
        SNAPSHOT_LISTENER_ARG = builder.define("snapshotListener", SnapshotListener.class).required().build();
        REFERENCE_SNAPSHOT_CONTROL_ARG = builder.define("referenceSnapshotControl", SnapshotControl.class).required().build();
        TARGET_SNAPSHOT_CONTROL_ARG = builder.define("targetSnapshotControl", SnapshotControl.class).required().build();
        OBJECT_CHANGE_FILTER_ARG = builder.define("objectChangeFilter", ObjectChangeFilter.class).required().build();
        COMPARE_CONTROL_ARG = builder.define("compareControl", CompareControl.class).required().build();
        OUTPUT_STREAM_ARG = builder.define("outputStream", PrintStream.class).required().build();
    }


    @Override
    public String[] getName() {
        return new String[]{"diff"};
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
    public void run(CommandScope commandScope) throws Exception {
        SnapshotCommand.logUnsupportedDatabase(REFERENCE_DATABASE_ARG.getValue(commandScope), this.getClass());

        DiffResult diffResult = createDiffResult(commandScope);

        new DiffToReport(diffResult, OUTPUT_STREAM_ARG.getValue(commandScope)).print();
    }

    public DiffResult createDiffResult(CommandScope commandScope) throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot referenceSnapshot = createReferenceSnapshot(commandScope);
        DatabaseSnapshot targetSnapshot = createTargetSnapshot(commandScope);

        final CompareControl compareControl = COMPARE_CONTROL_ARG.getValue(commandScope);
        referenceSnapshot.setSchemaComparisons(compareControl.getSchemaComparisons());
        if (targetSnapshot != null) {
            targetSnapshot.setSchemaComparisons(compareControl.getSchemaComparisons());
        }

        return DiffGeneratorFactory.getInstance().compare(referenceSnapshot, targetSnapshot, compareControl);
    }

    protected DatabaseSnapshot createTargetSnapshot(CommandScope commandScope) throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas;
        CompareControl compareControl = COMPARE_CONTROL_ARG.getValue(commandScope);
        Database targetDatabase = TARGET_DATABASE_ARG.getValue(commandScope);
        SnapshotControl snapshotControl = TARGET_SNAPSHOT_CONTROL_ARG.getValue(commandScope);
        Class<? extends DatabaseObject>[] snapshotTypes = SNAPSHOT_TYPES_ARG.getValue(commandScope);
        SnapshotListener snapshotListener = SNAPSHOT_LISTENER_ARG.getValue(commandScope);

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
        CompareControl compareControl = COMPARE_CONTROL_ARG.getValue(commandScope);
        Database targetDatabase = TARGET_DATABASE_ARG.getValue(commandScope);
        Database referenceDatabase = REFERENCE_DATABASE_ARG.getValue(commandScope);
        SnapshotControl snapshotControl = REFERENCE_SNAPSHOT_CONTROL_ARG.getValue(commandScope);
        Class<? extends DatabaseObject>[] snapshotTypes = SNAPSHOT_TYPES_ARG.getValue(commandScope);
        ObjectChangeFilter objectChangeFilter = OBJECT_CHANGE_FILTER_ARG.getValue(commandScope);
        SnapshotListener snapshotListener = SNAPSHOT_LISTENER_ARG.getValue(commandScope);

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

