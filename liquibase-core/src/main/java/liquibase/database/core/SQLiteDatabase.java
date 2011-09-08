package liquibase.database.core;

import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.database.AbstractDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.Column;
import liquibase.database.structure.Index;
import liquibase.database.structure.Table;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.util.ISODateFormat;

import java.math.BigInteger;
import java.util.*;

public class SQLiteDatabase extends AbstractDatabase {

    private Set<String> systemTables = new HashSet<String>();

    {
        systemTables.add("sqlite_sequence");
    }

    public static final String PRODUCT_NAME = "SQLite";

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        
        return "CURRENT_TIMESTAMP";
    }


    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sqlite:")) {
            return "SQLite.JDBCDriver";
        }
        return null;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    

    public String getTypeName() {
        return "sqlite";
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn)
            throws DatabaseException {
        return "SQLite".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public String getViewDefinition(String schemaName, String viewName) throws DatabaseException {
        return null;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    public String getTrigger(String table, String column) {
        return "CREATE TRIGGER insert_" + table + "_timeEnter AFTER  INSERT ON " + table + " BEGIN" +
                " UPDATE " + table + " SET " + column + " = DATETIME('NOW')" +
                " WHERE rowid = new.rowid END ";
    }

    @Override
    public String getAutoIncrementClause() {
        return "AUTOINCREMENT";
    }

    @Override
    protected boolean generateAutoIncrementStartWith(BigInteger startWith) {
    	// not supported
    	return false;
    }

    @Override
    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
    	// not supported
    	return false;
    }
    
    public static List<SqlStatement> getAlterTableStatements(
            AlterTableVisitor alterTableVisitor,
            Database database, String schemaName, String tableName)
            throws UnsupportedChangeException, DatabaseException {

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        DatabaseSnapshot snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);
        Table table = snapshot.getTable(tableName);

        List<ColumnConfig> createColumns = new Vector<ColumnConfig>();
        List<ColumnConfig> copyColumns = new Vector<ColumnConfig>();
        if (table != null) {
            for (Column column : table.getColumns()) {
                ColumnConfig new_column = new ColumnConfig(column);
                if (alterTableVisitor.createThisColumn(new_column)) {
                    createColumns.add(new_column);
                }
                ColumnConfig copy_column = new ColumnConfig(column);
                if (alterTableVisitor.copyThisColumn(copy_column)) {
                    copyColumns.add(copy_column);
                }
            }
        }
        for (ColumnConfig column : alterTableVisitor.getColumnsToAdd()) {
            ColumnConfig new_column = new ColumnConfig(column);
            if (alterTableVisitor.createThisColumn(new_column)) {
                createColumns.add(new_column);
            }
            ColumnConfig copy_column = new ColumnConfig(column);
            if (alterTableVisitor.copyThisColumn(copy_column)) {
                copyColumns.add(copy_column);
            }
        }

        List<Index> newIndices = new Vector<Index>();
        for (Index index : snapshot.getIndexes()) {
            if (index.getTable().getName().equalsIgnoreCase(tableName)) {
                if (alterTableVisitor.createThisIndex(index)) {
                    newIndices.add(index);
                }
            }
        }

        // rename table
        String temp_table_name = tableName + "_temporary";
        statements.add(new RenameTableStatement(schemaName, tableName, temp_table_name));
        // create temporary table
        CreateTableChange ct_change_tmp = new CreateTableChange();
        ct_change_tmp.setSchemaName(schemaName);
        ct_change_tmp.setTableName(tableName);
        for (ColumnConfig column : createColumns) {
            ct_change_tmp.addColumn(column);
        }
        statements.addAll(Arrays.asList(ct_change_tmp.generateStatements(database)));
        // copy rows to temporary table
        statements.add(new CopyRowsStatement(temp_table_name, tableName, copyColumns));
        // delete original table
        statements.add(new DropTableStatement(schemaName, temp_table_name, false));
        // validate indices
        statements.add(new ReindexStatement(schemaName, tableName));
        // add remaining indices
        for (Index index_config : newIndices) {
            statements.add(new CreateIndexStatement(
                    index_config.getName(),
                    schemaName, tableName,
                    index_config.isUnique(),
		            index_config.getAssociatedWithAsString(),
                    index_config.getColumns().
                            toArray(new String[index_config.getColumns().size()])));
        }

        return statements;
    }
    
    @Override
    protected Set<String> getSystemTablesAndViews() {
        return systemTables;
    }

    @Override
    public String getDateTimeLiteral(java.sql.Timestamp date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }


    public interface AlterTableVisitor {
        public ColumnConfig[] getColumnsToAdd();

        public boolean copyThisColumn(ColumnConfig column);

        public boolean createThisColumn(ColumnConfig column);

        public boolean createThisIndex(Index index);
    }

}
