package liquibase.migrator.diff;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Index;
import liquibase.database.structure.PrimaryKey;
import liquibase.database.structure.Sequence;
import liquibase.database.structure.Table;
import liquibase.database.structure.View;
import liquibase.migrator.exception.JDBCException;

public class Diff {

    private Database baseDatabase;
    private Database targetDatabase;

    private DatabaseSnapshot baseSnapshot;
    private DatabaseSnapshot targetSnapshot;

    private Set<DiffStatusListener> statusListeners = new HashSet<DiffStatusListener>();

    public void init(Connection baseConnection, Connection targetConnection) throws JDBCException {
        try {
            baseDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(baseConnection);
            baseDatabase.setConnection(baseConnection);
            baseDatabase.getConnection().setAutoCommit(false);

            targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(targetConnection);
            targetDatabase.setConnection(targetConnection);
            targetDatabase.getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public void init(Connection originalDatabase) throws JDBCException {
        try {
            targetDatabase = null;

            baseDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(originalDatabase);
            baseDatabase.setConnection(originalDatabase);
            baseDatabase.getConnection().setAutoCommit(false);

        } catch (SQLException e) {
            throw new JDBCException(e);
        }

    }

    public void addStatusListener(DiffStatusListener listener) {
        statusListeners.add(listener);
    }

    public void removeStatusListener(DiffStatusListener listener) {
        statusListeners.remove(listener);
    }

    public DiffResult compare() throws JDBCException {
        try {
            baseSnapshot = new DatabaseSnapshot(baseDatabase, statusListeners);
            if (targetDatabase == null) {
                targetSnapshot = new DatabaseSnapshot();
            } else {
                targetSnapshot = new DatabaseSnapshot(targetDatabase, statusListeners);

            }

            DiffResult diffResult = new DiffResult(baseDatabase, targetDatabase);
            checkVersionInfo(diffResult);
            checkTables(diffResult);
            checkViews(diffResult);
            checkColumns(diffResult);
            checkForeignKeys(diffResult);
            checkPrimaryKeys(diffResult);
            checkIndexes(diffResult);
            checkSequences(diffResult);

            return diffResult;
        } catch (SQLException e) {
            throw new JDBCException();
        }
    }

    private void checkVersionInfo(DiffResult diffResult) throws  JDBCException {

        if (targetDatabase != null) {
            diffResult.setProductName(new DiffComparison(baseDatabase.getDatabaseProductName(), targetDatabase.getDatabaseProductName()));
            diffResult.setProductVersion(new DiffComparison(baseDatabase.getDatabaseProductVersion(), targetDatabase.getDatabaseProductVersion()));
        }

    }

    private void checkTables(DiffResult diffResult) throws SQLException, JDBCException {
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

    private void checkViews(DiffResult diffResult)  {
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

        for (Sequence targetSequence: targetSnapshot.getSequences()) {
            if (!baseSnapshot.getSequences().contains(targetSequence)) {
                diffResult.addUnexpectedSequence(targetSequence);
            }
        }
    }
}
