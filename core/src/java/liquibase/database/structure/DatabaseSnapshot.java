package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.database.H2Database;
import liquibase.migrator.Migrator;
import liquibase.migrator.diff.DiffStatusListener;
import liquibase.migrator.exception.JDBCException;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Logger;

public class DatabaseSnapshot {

    private DatabaseMetaData databaseMetaData;
    private Database database;

    private Set<Table> tables = new HashSet<Table>();
    private Set<View> views = new HashSet<View>();
    private Set<Column> columns = new HashSet<Column>();
    private Set<ForeignKey> foreignKeys = new HashSet<ForeignKey>();
    private Set<Index> indexes = new HashSet<Index>();
    private Set<PrimaryKey> primaryKeys = new HashSet<PrimaryKey>();
    private Set<Sequence> sequences = new HashSet<Sequence>();


    private Map<String, Table> tablesMap = new HashMap<String, Table>();
    private Map<String, View> viewsMap = new HashMap<String, View>();
    private Map<String, Column> columnsMap = new HashMap<String, Column>();
    private Set<DiffStatusListener> statusListeners;

    private Logger log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);


    /**
     * Creates an empty database snapshot
     */
    public DatabaseSnapshot() {
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public DatabaseSnapshot(Database database) throws JDBCException {
        this(database,  null);
    }

    /**
     * Creates a snapshot of the given database.
     */
    public DatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        try {
            this.database = database;
            this.databaseMetaData = database.getConnection().getMetaData();
            this.statusListeners = statusListeners;

            readTablesAndViews();
            readColumns();
            readForeignKeyInformation();
            readPrimaryKeys();
            readIndexes();
            readSequences();

            this.tables = new HashSet<Table>(tablesMap.values());
            this.views = new HashSet<View>(viewsMap.values());
            this.columns = new HashSet<Column>(columnsMap.values());
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }


    public Set<Table> getTables() {
        return tables;
    }

    public Set<View> getViews() {
        return views;
    }

    public Set<Column> getColumns() {
        return columns;
    }

    public Set<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public Set<Index> getIndexes() {
        return indexes;
    }

    public Set<PrimaryKey> getPrimaryKeys() {
        return primaryKeys;
    }


    public Set<Sequence> getSequences() {
        return sequences;
    }

    private void readTablesAndViews() throws SQLException, JDBCException {
        updateListeners("Reading tables for "+database.toString()+" ...");
        ResultSet rs = databaseMetaData.getTables(database.getCatalogName(), database.getSchemaName(), null, new String[]{"TABLE", "VIEW"});
        while (rs.next()) {
            String type = rs.getString("TABLE_TYPE");
            String name = rs.getString("TABLE_NAME");
            String schemaName = rs.getString("TABLE_SCHEM");
            String catalogName = rs.getString("TABLE_CAT");

            if (database.isSystemTable(catalogName, schemaName, name) || database.isLiquibaseTable(name)) {
                continue;
            }

            if ("TABLE".equals(type)) {
                Table table = new Table();
                table.setName(name);
                tablesMap.put(name, table);
            } else if ("VIEW".equals(type)) {
                View view = new View();
                view.setName(name);
                viewsMap.put(name, view);
            }
        }
        rs.close();
    }

    private void readColumns() throws SQLException, JDBCException {
        updateListeners("Reading columns for "+database.toString()+" ...");

        ResultSet rs = databaseMetaData.getColumns(database.getCatalogName(), database.getSchemaName(), null, null);
        while (rs.next()) {
            Column columnInfo = new Column();

            String tableName = rs.getString("TABLE_NAME");
            String columnName = rs.getString("COLUMN_NAME");
            String schemaName = rs.getString("TABLE_SCHEM");
            String catalogName = rs.getString("TABLE_CAT");

            if (database.isSystemTable(catalogName, schemaName, tableName) || database.isLiquibaseTable(tableName)) {
                continue;
            }

            Table table = tablesMap.get(tableName);
            if (table == null) {
                View view = viewsMap.get(tableName);
                if (view == null) {
                    log.info("Could not find table or view " + tableName + " for column " + columnName);
                    continue;
                } else {
                    columnInfo.setView(view);
                    view.getColumns().add(columnInfo);
                }
            } else {
                columnInfo.setTable(table);
                table.getColumns().add(columnInfo);
            }

            columnInfo.setName(columnName);
            columnInfo.setDataType(rs.getInt("DATA_TYPE"));
            columnInfo.setColumnSize(rs.getInt("COLUMN_SIZE"));
            columnInfo.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
            columnInfo.setTypeName(rs.getString("TYPE_NAME"));
            String defaultValue = rs.getString("COLUMN_DEF");
            columnInfo.setAutoIncrement(isAutoIncrement(defaultValue, database));
            columnInfo.setDefaultValue(translateDefaultValue(defaultValue, database));

            int nullable = rs.getInt("NULLABLE");
            if (nullable == DatabaseMetaData.columnNoNulls) {
                columnInfo.setNullable(false);
            } else if (nullable == DatabaseMetaData.columnNullable) {
                columnInfo.setNullable(true);
            }

            columnsMap.put(columnName, columnInfo);
        }
        rs.close();
    }

    private boolean isAutoIncrement(String defaultValue, Database database) {
        if (database instanceof H2Database) {
            if (StringUtils.trimToEmpty(defaultValue).startsWith("(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_")) {
                return true;
            }
        }
        return false;
    }

    private String translateDefaultValue(String defaultValue, Database database) {
        if (database instanceof H2Database) {
            if (StringUtils.trimToEmpty(defaultValue).startsWith("(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_")) {
                return null;
            }
            return defaultValue;
        }
        return defaultValue;
    }

    private void readForeignKeyInformation() throws JDBCException, SQLException {
        updateListeners("Reading foreign keys for "+database.toString()+" ...");

        for (Table table : tablesMap.values()) {
            ResultSet rs = databaseMetaData.getExportedKeys(database.getCatalogName(), database.getSchemaName(), table.getName());
            while (rs.next()) {
                ForeignKey fkInfo = new ForeignKey();

                String pkTableName = rs.getString("PKTABLE_NAME");
                String pkColumn = rs.getString("PKCOLUMN_NAME");
                Table pkTable = tablesMap.get(pkTableName);
                if (pkTable == null) {
                    throw new JDBCException("Could not find table " + pkTableName + " for column " + pkColumn);
                }
                fkInfo.setPrimaryKeyTable(pkTable);
                fkInfo.setPrimaryKeyColumn(pkColumn);

                String fkTableName = rs.getString("FKTABLE_NAME");
                String fkColumn = rs.getString("FKCOLUMN_NAME");
                Table fkTable = tablesMap.get(fkTableName);
                if (fkTable == null) {
                    throw new JDBCException("Could not find table " + fkTableName + " for column " + fkColumn);
                }
                fkInfo.setForeignKeyTable(fkTable);
                fkInfo.setForeignKeyColumn(fkColumn);

                fkInfo.setName("FK_NAME");

                if (database.supportsInitiallyDeferrableColumns()) {
                    short deferrablility = rs.getShort("DEFERRABILITY");
                    if (deferrablility == DatabaseMetaData.importedKeyInitiallyDeferred) {
                        fkInfo.setDeferrable(Boolean.TRUE);
                        fkInfo.setInitiallyDeferred(Boolean.TRUE);
                    } else if (deferrablility == DatabaseMetaData.importedKeyInitiallyImmediate) {
                        fkInfo.setDeferrable(Boolean.TRUE);
                        fkInfo.setInitiallyDeferred(Boolean.FALSE);
                    } else if (deferrablility == DatabaseMetaData.importedKeyNotDeferrable) {
                        fkInfo.setDeferrable(Boolean.FALSE);
                        fkInfo.setInitiallyDeferred(Boolean.FALSE);
                    }
                }


                foreignKeys.add(fkInfo);
            }

            rs.close();
        }
    }

    private void readIndexes() throws JDBCException, SQLException {
        updateListeners("Reading indexes for "+database.toString()+" ...");

        for (Table table : tablesMap.values()) {
            ResultSet rs;
            try {
                rs = databaseMetaData.getIndexInfo(database.getCatalogName(), database.getSchemaName(), table.getName(), true, true);
            } catch (SQLException e) {
                throw e;
            }

            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                short type = rs.getShort("TYPE");
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");

                boolean isPKIndex = false;
                for (PrimaryKey pk : primaryKeys) {
                    if (pk.getTableName().equalsIgnoreCase(tableName)
                            && pk.getColumnNames().equalsIgnoreCase(columnName)) {
                        isPKIndex = true;
                        break;
                    }
                }

                if (isPKIndex || type == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }

                Index indexInformation = new Index();
                indexInformation.setTableName(tableName);
                if (columnName == null) {
                    //nothing to index, not sure why these come through sometimes
                    continue;
                }
                indexInformation.setColumnName(columnName);
                indexInformation.setName(indexName);

                indexes.add(indexInformation);
            }

            rs.close();
        }
    }

    private void readPrimaryKeys() throws JDBCException, SQLException {
        updateListeners("Reading primary keys for "+database.toString()+" ...");

        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();

        for (Table table : tablesMap.values()) {
            ResultSet rs = databaseMetaData.getPrimaryKeys(database.getCatalogName(), database.getSchemaName(), table.getName());

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");

                boolean foundExistingPK = false;
                for (PrimaryKey pk : foundPKs) {
                    if (pk.getTableName().equals(tableName)) {
                        pk.addColumnName(columnName);

                        foundExistingPK = true;
                    }
                }

                if (!foundExistingPK) {
                    PrimaryKey primaryKey = new PrimaryKey();
                    primaryKey.setTableName(tableName);
                    primaryKey.addColumnName(columnName);
                    primaryKey.setName(rs.getString("PK_NAME"));

                    foundPKs.add(primaryKey);
                }
            }

            rs.close();
        }

        this.primaryKeys.addAll(foundPKs);
    }

    private void readSequences() throws JDBCException, SQLException {
        updateListeners("Reading sequences for "+database.toString()+" ...");

        if (database.supportsSequences()) {
            Statement stmt = database.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(database.createFindSequencesSQL());

            while (rs.next()) {
                Sequence seq = new Sequence();
                seq.setName(rs.getString("SEQUENCE_NAME"));

                sequences.add(seq);
            }

            rs.close();
        }
    }

    private void updateListeners(String message) {
        if (this.statusListeners == null) {
            return;
        }
        for (DiffStatusListener listener : this.statusListeners) {
            listener.statusUpdate(message);
        }
    }
}
