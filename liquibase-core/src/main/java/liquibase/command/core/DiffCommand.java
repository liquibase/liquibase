package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
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
import liquibase.util.StringUtils;

import java.io.PrintStream;
import java.util.Set;

public class DiffCommand extends AbstractCommand<CommandResult> {

    private Database referenceDatabase;
    private Database targetDatabase;
    private Class<? extends DatabaseObject>[] snapshotTypes;
    private PrintStream outputStream;
    private SnapshotListener snapshotListener;
    private SnapshotControl referenceSnapshotControl;
    private SnapshotControl targetSnapshotControl;
    private ObjectChangeFilter objectChangeFilter;
    private CompareControl compareControl;


    @Override
    public String getName() {
        return "diff";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public Database getReferenceDatabase() {
        return referenceDatabase;
    }

    public DiffCommand setReferenceDatabase(Database referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
        return this;
    }

    public Database getTargetDatabase() {
        return targetDatabase;
    }

    public DiffCommand setTargetDatabase(Database targetDatabase) {
        this.targetDatabase = targetDatabase;
        return this;
    }

    public Class<? extends DatabaseObject>[] getSnapshotTypes() {
        return snapshotTypes;
    }

    public DiffCommand setSnapshotTypes(String... snapshotTypes) {
        if ((snapshotTypes == null) || (snapshotTypes.length == 0) || (snapshotTypes[0] == null)) {
            this.snapshotTypes = null;
            return this;
        }

        Set<Class<? extends DatabaseObject>> types = DatabaseObjectFactory.getInstance().parseTypes(StringUtils.join(snapshotTypes, ","));
        this.snapshotTypes = new Class[types.size()];
        int i = 0;
        for (Class<? extends DatabaseObject> type : types) {
            this.snapshotTypes[i++] = type;
        }
        return this;
    }

    public DiffCommand setSnapshotTypes(Class<? extends DatabaseObject>... snapshotTypes) {
        this.snapshotTypes = snapshotTypes;
        return this;
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public DiffCommand setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }

    public SnapshotControl getReferenceSnapshotControl() {
        return referenceSnapshotControl;
    }

    public DiffCommand setReferenceSnapshotControl(SnapshotControl referenceSnapshotControl) {
        this.referenceSnapshotControl = referenceSnapshotControl;
        return this;
    }

    public SnapshotControl getTargetSnapshotControl() {
        return targetSnapshotControl;
    }

    public DiffCommand setTargetSnapshotControl(SnapshotControl targetSnapshotControl) {
        this.targetSnapshotControl = targetSnapshotControl;
        return this;
    }

    public SnapshotListener getSnapshotListener() {
        return snapshotListener;
    }

    public DiffCommand setSnapshotListener(SnapshotListener snapshotListener) {
        this.snapshotListener = snapshotListener;
        return this;
    }

    public CompareControl getCompareControl() {
        return compareControl;
    }

    public DiffCommand setCompareControl(CompareControl compareControl) {
        this.compareControl = compareControl;
        return this;
    }

    @Override
    protected CommandResult run() throws Exception {
        DiffResult diffResult = createDiffResult();

        new DiffToReport(diffResult, outputStream).print();

        return new CommandResult("OK");
    }

    protected DiffResult createDiffResult() throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot referenceSnapshot = createReferenceSnapshot();
        DatabaseSnapshot targetSnapshot = createTargetSnapshot();

        referenceSnapshot.setSchemaComparisons(compareControl.getSchemaComparisons());
        if (targetSnapshot != null) {
            targetSnapshot.setSchemaComparisons(compareControl.getSchemaComparisons());
        }

        return DiffGeneratorFactory.getInstance().compare(referenceSnapshot, targetSnapshot, compareControl);
    }

    protected DatabaseSnapshot createTargetSnapshot() throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas;

        if ((compareControl == null) || (compareControl.getSchemaComparisons() == null)) {
            schemas = new CatalogAndSchema[]{targetDatabase.getDefaultSchema()};
        } else {
            schemas =new CatalogAndSchema[compareControl.getSchemaComparisons().length];

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
        SnapshotControl snapshotControl = getTargetSnapshotControl();
        if (snapshotControl == null) {
            snapshotControl = new SnapshotControl(targetDatabase, snapshotTypes);
        }
        if (getSnapshotListener() != null) {
            snapshotControl.setSnapshotListener(getSnapshotListener());
        }
        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
            referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            return SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, targetDatabase, snapshotControl);
        } finally {
            referenceDatabase.setObjectQuotingStrategy(originalStrategy);
        }
    }

    protected DatabaseSnapshot createReferenceSnapshot() throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas;

        if ((compareControl == null) || (compareControl.getSchemaComparisons() == null)) {
            schemas = new CatalogAndSchema[]{targetDatabase.getDefaultSchema()};
        } else {
            schemas =new CatalogAndSchema[compareControl.getSchemaComparisons().length];

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

        SnapshotControl snapshotControl = getReferenceSnapshotControl();
        if (snapshotControl == null) {
            snapshotControl = new SnapshotControl(referenceDatabase, objectChangeFilter, snapshotTypes);
        }
        if (getSnapshotListener() != null) {
            snapshotControl.setSnapshotListener(getSnapshotListener());
        }

        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
            referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            return SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, referenceDatabase, snapshotControl);
        } finally {
            referenceDatabase.setObjectQuotingStrategy(originalStrategy);
        }
    }

    public DiffCommand setObjectChangeFilter(ObjectChangeFilter objectChangeFilter) {
        this.objectChangeFilter = objectChangeFilter;
        return this;
    }
}

