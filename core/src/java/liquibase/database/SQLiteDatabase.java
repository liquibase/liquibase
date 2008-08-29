package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import liquibase.change.ColumnConfig;
import liquibase.change.CreateTableChange;
//import liquibase.database.sql.CopyRowsStatement;
import liquibase.database.sql.CopyRowsStatement;
import liquibase.database.sql.CreateIndexStatement;
import liquibase.database.sql.DropTableStatement;
import liquibase.database.sql.ReindexStatement;
//import liquibase.database.sql.ReindexStatement;
import liquibase.database.sql.RenameTableStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Index;
import liquibase.database.structure.SQLiteDatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.ISODateFormat;

public class SQLiteDatabase extends AbstractDatabase {

    private Set<String> systemTables = new HashSet<String>();

    {
        systemTables.add("sqlite_sequence");
    }

    public static final String PRODUCT_NAME = "SQLite";

    public String getBlobType() {
        return "BLOB";
    }

    public String getBooleanType() {
        return "BOOLEAN";
    }

    public String getClobType() {
        return "TEXT";
    }

    public String getCurrencyType() {
        return "REAL";
    }

    public String getCurrentDateTimeFunction() {
        return "CURRENT_TIMESTAMP";
    }

    public String getDateTimeType() {
        return "TEXT";
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sqlite:")) {
            return "SQLite.JDBCDriver";
        }
        return null;
    }

    public String getProductName() {
        return "SQLite";
    }

    public String getTypeName() {
        return "sqlite";
    }

    public String getUUIDType() {
        return "TEXT";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn)
            throws JDBCException {
        return "SQLite".equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    public String getViewDefinition(String schemaName, String viewName) throws JDBCException {
        return null;
    }

    public boolean supportsSequences() {
        return false;
    }

    public boolean supportsSchemas() {
        return false;
    }

    public String getFalseBooleanValue() {
        return "0";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    public String getTrigger(String table, String column) {
        return "CREATE TRIGGER insert_" + table + "_timeEnter AFTER  INSERT ON " + table + " BEGIN" +
                " UPDATE " + table + " SET " + column + " = DATETIME('NOW')" +
                " WHERE rowid = new.rowid END ";
    }

    public String getAutoIncrementClause() {
        return "";
    }

    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type;
        if (columnType.equals("INTEGER") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("int") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("bit")) {
            type = "INTEGER";
        } else if (columnType.equals("TEXT") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("uuid") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("uniqueidentifier") ||

                columnType.toLowerCase(Locale.ENGLISH).equals("uniqueidentifier") ||
                columnType.toLowerCase(Locale.ENGLISH).equals("datetime") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("timestamp") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("char") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("clob") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("text")) {
            type = "TEXT";
        } else if (columnType.equals("REAL") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("float")) {
            type = "REAL";
        } else if (columnType.toLowerCase(Locale.ENGLISH).contains("blob") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("binary")) {
            type = "BLOB";
        } else if (columnType.toLowerCase(Locale.ENGLISH).contains("boolean") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("binary")) {
            type = "BOOLEAN";
        } else {
            type = super.getColumnType(columnType, autoIncrement);
        }
        return type;
    }

    public static Collection<SqlStatement> getAlterTableStatements(
            AlterTableVisitor alterTableVisitor,
            Database database, String schemaName, String tableName)
            throws UnsupportedChangeException, JDBCException {

        Collection<SqlStatement> statements = new Vector<SqlStatement>();

        DatabaseSnapshot snapshot = new SQLiteDatabaseSnapshot(database);
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
                    index_config.getColumns().
                            toArray(new String[index_config.getColumns().size()])));
        }

        return statements;
    }

    public String getConnectionUsername() throws JDBCException {
        try {
            String username = getConnection().getMetaData().getUserName();
            if (username == null) {
                username = "liquibase";
            }
            return username;
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    protected Set<String> getSystemTablesAndViews() {
        return systemTables;
    }

    public String getDateLiteral(java.sql.Timestamp date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }


    public interface AlterTableVisitor {
        public ColumnConfig[] getColumnsToAdd();

        public boolean copyThisColumn(ColumnConfig column);

        public boolean createThisColumn(ColumnConfig column);

        public boolean createThisIndex(Index index);
    }

    public DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new SQLiteDatabaseSnapshot(this);
    }

}
