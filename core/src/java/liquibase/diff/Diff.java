package liquibase.diff;

import liquibase.database.Database;
import liquibase.database.structure.*;
import liquibase.exception.JDBCException;
import liquibase.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class Diff {

    private Database baseDatabase;
    private Database targetDatabase;

    private DatabaseSnapshot baseSnapshot;
    private DatabaseSnapshot targetSnapshot;

    private Set<DiffStatusListener> statusListeners = new HashSet<DiffStatusListener>();

    private boolean diffTables = true;
    private boolean diffColumns = true;
    private boolean diffViews = true;
    private boolean diffPrimaryKeys = true;
    private boolean diffIndexes = true;
    private boolean diffForeignKeys = true;
    private boolean diffSequences = true;
    private boolean diffData = false;


    public Diff(Database baseDatabase, Database targetDatabase) {
        this.baseDatabase = baseDatabase;

        this.targetDatabase = targetDatabase;
    }

    public Diff(Database originalDatabase, String schema) throws JDBCException {
        targetDatabase = null;

        baseDatabase = originalDatabase;
        baseDatabase.setDefaultSchemaName(schema);
    }

    public Diff(DatabaseSnapshot baseDatabaseSnapshot, DatabaseSnapshot targetDatabaseSnapshot) {
        this.baseSnapshot = baseDatabaseSnapshot;

        this.targetSnapshot = targetDatabaseSnapshot;
    }

    public void addStatusListener(DiffStatusListener listener) {
        statusListeners.add(listener);
    }

    public void removeStatusListener(DiffStatusListener listener) {
        statusListeners.remove(listener);
    }

    public DiffResult compare() throws JDBCException {
        if (baseSnapshot == null) {
            baseSnapshot = baseDatabase.createDatabaseSnapshot(null, statusListeners);
        }

        if (targetSnapshot == null) {
            if (targetDatabase == null) {
                targetSnapshot = new SqlDatabaseSnapshot();
            } else {
                targetSnapshot = targetDatabase.createDatabaseSnapshot(null, statusListeners);
            }
        }

        DiffResult diffResult = new DiffResult(baseSnapshot, targetSnapshot);
        checkVersionInfo(diffResult);
        if (shouldDiffTables()) {
            checkTables(diffResult);
        }
        if (shouldDiffViews()) {
            checkViews(diffResult);
        }
        if (shouldDiffColumns()) {
            checkColumns(diffResult);
        }
        if (shouldDiffForeignKeys()) {
            checkForeignKeys(diffResult);
        }
        if (shouldDiffPrimaryKeys()) {
            checkPrimaryKeys(diffResult);
        }
        if (shouldDiffIndexes()) {
            checkIndexes(diffResult);
        }
        if (shouldDiffSequences()) {
            checkSequences(diffResult);
        }
        diffResult.setDiffData(shouldDiffData());

        return diffResult;
    }


    public void setDiffTypes(String diffTypes) {
        if (StringUtils.trimToNull(diffTypes) != null) {
            Set<String> types = new HashSet<String>(Arrays.asList(diffTypes.toLowerCase().split("\\s*,\\s*")));
            diffTables = types.contains("tables");
            diffColumns = types.contains("columns");
            diffViews = types.contains("views");
            diffPrimaryKeys = types.contains("primaryKeys");
            diffIndexes = types.contains("indexes");
            diffForeignKeys = types.contains("foreignKeys");
            diffSequences = types.contains("sequences");
            diffData = types.contains("data");            
        }
    }

    public boolean shouldDiffTables() {
        return diffTables;
    }

    public void setDiffTables(boolean diffTables) {
        this.diffTables = diffTables;
    }

    public boolean shouldDiffColumns() {
        return diffColumns;
    }

    public void setDiffColumns(boolean diffColumns) {
        this.diffColumns = diffColumns;
    }

    public boolean shouldDiffViews() {
        return diffViews;
    }

    public void setDiffViews(boolean diffViews) {
        this.diffViews = diffViews;
    }


    public boolean shouldDiffPrimaryKeys() {
        return diffPrimaryKeys;
    }

    public void setDiffPrimaryKeys(boolean diffPrimaryKeys) {
        this.diffPrimaryKeys = diffPrimaryKeys;
    }

    public boolean shouldDiffIndexes() {
        return diffIndexes;
    }

    public void setDiffIndexes(boolean diffIndexes) {
        this.diffIndexes = diffIndexes;
    }

    public boolean shouldDiffForeignKeys() {
        return diffForeignKeys;
    }

    public void setDiffForeignKeys(boolean diffForeignKeys) {
        this.diffForeignKeys = diffForeignKeys;
    }

    public boolean shouldDiffSequences() {
        return diffSequences;
    }

    public void setDiffSequences(boolean diffSequences) {
        this.diffSequences = diffSequences;
    }

    public boolean shouldDiffData() {
        return diffData;
    }

    public void setDiffData(boolean diffData) {
        this.diffData = diffData;
    }

    private void checkVersionInfo(DiffResult diffResult) throws JDBCException {

        if (targetDatabase != null) {
            diffResult.setProductName(new DiffComparison(baseDatabase.getDatabaseProductName(), targetDatabase.getDatabaseProductName()));
            diffResult.setProductVersion(new DiffComparison(baseDatabase.getDatabaseProductVersion(), targetDatabase.getDatabaseProductVersion()));
        }

    }

    private void checkTables(DiffResult diffResult) {
        for (Table baseTable : baseSnapshot.getTables()) {
            if (!targetSnapshot.getTables().contains(baseTable)) {
                diffResult.addMissingTable(baseTable);
            }
        }

        for (Table targetTable : targetSnapshot.getTables()) {
            if (!baseSnapshot.getTables().contains(targetTable)) {
                diffResult.addUnexpectedTable(targetTable);
            }
        }
    }

    private void checkViews(DiffResult diffResult) {
        for (View baseView : baseSnapshot.getViews()) {
            if (!targetSnapshot.getViews().contains(baseView)) {
                diffResult.addMissingView(baseView);
            }
        }

        for (View targetView : targetSnapshot.getViews()) {
            if (!baseSnapshot.getViews().contains(targetView)) {
                diffResult.addUnexpectedView(targetView);
            }
        }
    }

    private void checkColumns(DiffResult diffResult) {
        for (Column baseColumn : baseSnapshot.getColumns()) {
            if (!targetSnapshot.getColumns().contains(baseColumn)
                    && (baseColumn.getTable() == null || !diffResult.getMissingTables().contains(baseColumn.getTable()))
                    && (baseColumn.getView() == null || !diffResult.getMissingViews().contains(baseColumn.getView()))
                    ) {
                diffResult.addMissingColumn(baseColumn);
            }
        }

        for (Column targetColumn : targetSnapshot.getColumns()) {
            if (!baseSnapshot.getColumns().contains(targetColumn)
                    && (targetColumn.getTable() == null || !diffResult.getUnexpectedTables().contains(targetColumn.getTable()))
                    && (targetColumn.getView() == null || !diffResult.getUnexpectedViews().contains(targetColumn.getView()))
                    ) {
                diffResult.addUnexpectedColumn(targetColumn);
            } else
            if (targetColumn.getTable() != null && !diffResult.getUnexpectedTables().contains(targetColumn.getTable())) {
                Column baseColumn = baseSnapshot.getColumn(targetColumn.getTable().getName(), targetColumn.getName());

                if (baseColumn == null || targetColumn.isDifferent(baseColumn)) {
                    diffResult.addChangedColumn(targetColumn);
                }
            }
        }
    }

    private void checkForeignKeys(DiffResult diffResult) {
        for (ForeignKey baseFK : baseSnapshot.getForeignKeys()) {
            if (!targetSnapshot.getForeignKeys().contains(baseFK)) {
                diffResult.addMissingForeignKey(baseFK);
            }
        }

        for (ForeignKey targetFK : targetSnapshot.getForeignKeys()) {
            if (!baseSnapshot.getForeignKeys().contains(targetFK)) {
                diffResult.addUnexpectedForeignKey(targetFK);
            }
        }
    }

    private void checkIndexes(DiffResult diffResult) {
        for (Index baseIndex : baseSnapshot.getIndexes()) {
            if (!targetSnapshot.getIndexes().contains(baseIndex)) {
                diffResult.addMissingIndex(baseIndex);
            }
        }

        for (Index targetIndex : targetSnapshot.getIndexes()) {
            if (!baseSnapshot.getIndexes().contains(targetIndex)) {
                diffResult.addUnexpectedIndex(targetIndex);
            }
        }
    }

    private void checkPrimaryKeys(DiffResult diffResult) {
        for (PrimaryKey basePrimaryKey : baseSnapshot.getPrimaryKeys()) {
            if (!targetSnapshot.getPrimaryKeys().contains(basePrimaryKey)) {
                diffResult.addMissingPrimaryKey(basePrimaryKey);
            }
        }

        for (PrimaryKey targetPrimaryKey : targetSnapshot.getPrimaryKeys()) {
            if (!baseSnapshot.getPrimaryKeys().contains(targetPrimaryKey)) {
                diffResult.addUnexpectedPrimaryKey(targetPrimaryKey);
            }
        }
    }

    private void checkSequences(DiffResult diffResult) {
        for (Sequence baseSequence : baseSnapshot.getSequences()) {
            if (!targetSnapshot.getSequences().contains(baseSequence)) {
                diffResult.addMissingSequence(baseSequence);
            }
        }

        for (Sequence targetSequence : targetSnapshot.getSequences()) {
            if (!baseSnapshot.getSequences().contains(targetSequence)) {
                diffResult.addUnexpectedSequence(targetSequence);
            }
        }
    }
}
