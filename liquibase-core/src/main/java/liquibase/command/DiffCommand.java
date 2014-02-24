package liquibase.command;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.report.DiffToReport;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;
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
    private CompareControl compareControl;
    private CatalogAndSchema[] referenceSchemas;
    private CatalogAndSchema[] targetSchemas;


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

    public CompareControl getCompareControl() {
        return compareControl;
    }

    public DiffCommand setCompareControl(CompareControl compareControl) {
        this.compareControl = compareControl;
        return this;
    }

    public CatalogAndSchema[] getReferenceSchemas() {
        return referenceSchemas;
    }

    public DiffCommand setReferenceSchemas(CatalogAndSchema[] referenceSchemas) {
        this.referenceSchemas = referenceSchemas;
        return this;
    }

    public CatalogAndSchema[] getTargetSchemas() {
        return targetSchemas;
    }

    public DiffCommand setTargetSchemas(CatalogAndSchema[] targetSchemas) {
        this.targetSchemas = targetSchemas;
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

        CompareControl compareControl = new CompareControl(referenceSnapshot.getSnapshotControl().getTypesToInclude());
        return DiffGeneratorFactory.getInstance().compare(referenceSnapshot, targetSnapshot, compareControl);
    }

    protected DatabaseSnapshot createTargetSnapshot() throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas = getTargetSchemas();
        if (schemas == null) {
            schemas = new CatalogAndSchema[] {targetDatabase.getDefaultSchema()};
        }

        return SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, targetDatabase, new SnapshotControl(targetDatabase, snapshotTypes));
    }

    protected DatabaseSnapshot createReferenceSnapshot() throws DatabaseException, InvalidExampleException {
        CatalogAndSchema[] schemas = getReferenceSchemas();
        if (schemas == null) {
            schemas = new CatalogAndSchema[] {referenceDatabase.getDefaultSchema()};
        }

        return SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, referenceDatabase, new SnapshotControl(referenceDatabase, snapshotTypes));
    }
}
