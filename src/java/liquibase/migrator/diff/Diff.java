package liquibase.migrator.diff;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.*;
import liquibase.migrator.diff.emptydatabase.NullDatabase;
import liquibase.migrator.exception.JDBCException;

import java.sql.Connection;
import java.sql.SQLException;

public class Diff {

    private Database baseDatabase;
    private Database targetDatabase;

    private DatabaseSnapshot baseSnapshot;
    private DatabaseSnapshot targetSnapshot;

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
            targetDatabase = new NullDatabase();

            baseDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(originalDatabase);
            baseDatabase.setConnection(originalDatabase);
            baseDatabase.getConnection().setAutoCommit(false);

        } catch (SQLException e) {
            throw new JDBCException(e);
        }

    }


    public DiffResult compare() throws JDBCException {
        try {
            baseSnapshot = new DatabaseSnapshot(baseDatabase);
            targetSnapshot = new DatabaseSnapshot(targetDatabase);

            DiffResult diffResult = new DiffResult(baseDatabase, targetDatabase);
            checkVersionInfo(diffResult);
            checkTables(diffResult);
            checkViews(diffResult);
            checkColumns(diffResult);
            checkForeignKeys(diffResult);
            checkPrimaryKeys(diffResult);
            checkIndexes(diffResult);

            return diffResult;
        } catch (SQLException e) {
            throw new JDBCException();
        }
    }

    private void checkVersionInfo(DiffResult diffResult) throws SQLException, JDBCException {

        diffResult.setProductName(new DiffComparison(baseDatabase.getDatabaseProductName(), targetDatabase.getDatabaseProductName()));
        diffResult.setProductVersion(new DiffComparison(baseDatabase.getDatabaseProductVersion(), targetDatabase.getDatabaseProductVersion()));

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

    private void checkViews(DiffResult diffResult) throws SQLException, JDBCException {
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
}
