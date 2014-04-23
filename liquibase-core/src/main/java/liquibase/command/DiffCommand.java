package liquibase.command;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.report.DiffToReport;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class DiffCommand extends AbstractCommand {

    private Database referenceDatabase;
    private Database targetDatabase;
    private Class<? extends DatabaseObject>[] snapshotTypes;
    private PrintStream outputStream;
    private SnapshotListener snapshotListener;
    private SnapshotControl referenceSnapshotControl;
    private SnapshotControl targetSnapshotControl;
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
        if (snapshotTypes == null || snapshotTypes.length == 0 || snapshotTypes[0] == null) {
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
    protected Object run() throws Exception {
        DiffResult diffResult = createDiffResult();

        new DiffToReport(diffResult, outputStream).print();

        return null;
    }

    protected DiffResult createDiffResult() throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot referenceSnapshot = createReferenceSnapshot();
        DatabaseSnapshot targetSnapshot = createTargetSnapshot();

        return DiffGeneratorFactory.getInstance().compare(referenceSnapshot, targetSnapshot, compareControl);
    }

    protected DatabaseSnapshot createTargetSnapshot() throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas;

        if (compareControl == null || compareControl.getSchemaComparisons() == null) {
            schemas = new CatalogAndSchema[]{targetDatabase.getDefaultSchema()};
        } else {
            schemas =new CatalogAndSchema[compareControl.getSchemaComparisons().length];

            int i = 0;
            for (CompareControl.SchemaComparison comparison : compareControl.getSchemaComparisons()) {
                schemas[i++] = comparison.getComparisonSchema();
            }
        }
        SnapshotControl snapshotControl = getTargetSnapshotControl();
        if (snapshotControl == null) {
            snapshotControl = new SnapshotControl(targetDatabase, snapshotTypes);
        }
        if (getSnapshotListener() != null) {
            snapshotControl.setSnapshotListener(getSnapshotListener());
        }
        return SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, targetDatabase, snapshotControl);
    }

    protected DatabaseSnapshot createReferenceSnapshot() throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas;

        if (compareControl == null || compareControl.getSchemaComparisons() == null) {
            schemas = new CatalogAndSchema[]{targetDatabase.getDefaultSchema()};
        } else {
            schemas =new CatalogAndSchema[compareControl.getSchemaComparisons().length];

            int i = 0;
            for (CompareControl.SchemaComparison comparison : compareControl.getSchemaComparisons()) {
                schemas[i++] = comparison.getReferenceSchema();
            }
        }

        SnapshotControl snapshotControl = getReferenceSnapshotControl();
        if (snapshotControl == null) {
            snapshotControl = new SnapshotControl(referenceDatabase, snapshotTypes);
        }
        if (getSnapshotListener() != null) {
            snapshotControl.setSnapshotListener(getSnapshotListener());
        }
        return SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, referenceDatabase, snapshotControl);
    }
}

