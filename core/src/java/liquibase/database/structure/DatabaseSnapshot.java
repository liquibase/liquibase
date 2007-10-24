package liquibase.database.structure;

import liquibase.database.*;
import liquibase.database.template.JdbcTemplate;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;
import liquibase.migrator.Migrator;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.text.ParseException;

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

    private static final Logger log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);


    /**
     * Creates an empty database snapshot
     */
    public DatabaseSnapshot() {
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public DatabaseSnapshot(Database database) throws JDBCException {
        this(database, null);
    }

    /**
     * Creates a snapshot of the given database.
     */
    public DatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        this(database, statusListeners, null);
    }

    /**
     * Creates a snapshot of the given database.
     */
    public DatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        try {
            this.database = database;
            this.databaseMetaData = database.getConnection().getMetaData();
            this.statusListeners = statusListeners;

            readTablesAndViews(requestedSchema);
            readForeignKeyInformation(requestedSchema);
            readPrimaryKeys(requestedSchema);
            readColumns(requestedSchema);
//            readUniqueConstraints(catalog, schema);
            readIndexes(requestedSchema);
            readSequences(requestedSchema);

            this.tables = new HashSet<Table>(tablesMap.values());
            this.views = new HashSet<View>(viewsMap.values());
            this.columns = new HashSet<Column>(columnsMap.values());
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }


    public Database getDatabase() {
        return database;
    }

    public Set<Table> getTables() {
        return tables;
    }

    public Set<View> getViews() {
        return views;
    }

    public Column getColumn(Column column) {
        if (column.getTable() == null) {
            return columnsMap.get(column.getView().getName() + "." + column.getName());
        } else {
            return columnsMap.get(column.getTable().getName() + "." + column.getName());
        }
    }

    public Column getColumn(String tableName, String columnName) {
        return columnsMap.get(tableName + "." + columnName);
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

    private void readTablesAndViews(String schema) throws SQLException, JDBCException {
        updateListeners("Reading tables for " + database.toString() + " ...");
        ResultSet rs = databaseMetaData.getTables(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, new String[]{"TABLE", "VIEW"});
        while (rs.next()) {
            String type = rs.getString("TABLE_TYPE");
            String name = rs.getString("TABLE_NAME");
            String schemaName = rs.getString("TABLE_SCHEM");
            String catalogName = rs.getString("TABLE_CAT");

            if (database.isSystemTable(catalogName, schemaName, name) || database.isLiquibaseTable(name) || database.isSystemView(catalogName, schemaName, name)) {
                continue;
            }

            if ("TABLE".equals(type)) {
                Table table = new Table(name);
                tablesMap.put(name, table);
            } else if ("VIEW".equals(type)) {
                View view = new View();
                view.setName(name);
                try {
                    view.setDefinition(database.getViewDefinition(schema, name));
                } catch (JDBCException e) {
                    System.out.println("Error getting view with "+ ((AbstractDatabase) database).getViewDefinitionSql(schema, name));
                    throw e;
                }

                viewsMap.put(name, view);

            }
        }
        rs.close();
    }

    private void readColumns(String schema) throws SQLException, JDBCException {
        updateListeners("Reading columns for " + database.toString() + " ...");

        Statement selectStatement = database.getConnection().createStatement();
        ResultSet rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, null);
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
            Object defaultValue = rs.getObject("COLUMN_DEF");
            try {
                columnInfo.setDefaultValue(database.convertDatabaseValueToJavaObject(defaultValue, columnInfo.getDataType(), columnInfo.getColumnSize(), columnInfo.getDecimalDigits()));
            } catch (ParseException e) {
                throw new JDBCException(e);
            }

            int nullable = rs.getInt("NULLABLE");
            if (nullable == DatabaseMetaData.columnNoNulls) {
                columnInfo.setNullable(false);
            } else if (nullable == DatabaseMetaData.columnNullable) {
                columnInfo.setNullable(true);
            }

            columnInfo.setPrimaryKey(isPrimaryKey(columnInfo));

            ResultSet selectRS = null;
            try {
                selectRS = selectStatement.executeQuery("SELECT " + columnName + " FROM " + database.escapeTableName(schema, tableName) + " WHERE 1 = 0");
                ResultSetMetaData meta = selectRS.getMetaData();
                columnInfo.setAutoIncrement(meta.isAutoIncrement(1));
            } catch (SQLException e) {
                throw e;
            } finally {
                if (selectRS != null) {
                    selectRS.close();
                }
            }


            columnsMap.put(tableName + "." + columnName, columnInfo);
        }
        rs.close();
        selectStatement.close();
    }

    private boolean isPrimaryKey(Column columnInfo) {
        for (PrimaryKey pk : getPrimaryKeys()) {
            if (pk.getTableName().equalsIgnoreCase(pk.getTableName())) {
                if (pk.getColumnNamesAsList().contains(columnInfo.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    private void readForeignKeyInformation(String schema) throws JDBCException, SQLException {
        updateListeners("Reading foreign keys for " + database.toString() + " ...");

        for (Table table : tablesMap.values()) {
            ResultSet rs = databaseMetaData.getExportedKeys(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), table.getName());
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

                fkInfo.setName(rs.getString("FK_NAME"));

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

    private void readIndexes(String schema) throws JDBCException, SQLException {
        updateListeners("Reading indexes for " + database.toString() + " ...");

        for (Table table : tablesMap.values()) {
            ResultSet rs;
            rs = databaseMetaData.getIndexInfo(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), table.getName(), false, true);
            Map<String, Index> indexMap = new HashMap<String, Index>();
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                short type = rs.getShort("TYPE");
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                short position = rs.getShort("ORDINAL_POSITION");
                String filterCondition = rs.getString("FILTER_CONDITION");

                if (type == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }

                if (columnName == null) {
                    //nothing to index, not sure why these come through sometimes
                    continue;
                }
                Index indexInformation;
                if (indexMap.containsKey(indexName)) {
                    indexInformation = indexMap.get(indexName);
                } else {
                    indexInformation = new Index();
                    indexInformation.setTableName(tableName);
                    indexInformation.setName(indexName);
                    indexInformation.setFilterCondition(filterCondition);
                    indexMap.put(indexName, indexInformation);
                }
                indexInformation.getColumns().add(position - 1, columnName);
            }
            for (String key : indexMap.keySet()) {
                indexes.add(indexMap.get(key));
            }
            rs.close();
        }

        Set<Index> indexesToRemove = new HashSet<Index>();
        //remove PK indexes
        for (Index index : indexes) {
            for (PrimaryKey pk : primaryKeys) {
                if (index.getTableName().equalsIgnoreCase(pk.getTableName())
                        && index.getColumnNames().equals(pk.getColumnNames())) {
                    indexesToRemove.add(index);
                }
            }
        }
        indexes.removeAll(indexesToRemove);
    }

    private void readPrimaryKeys(String schema) throws JDBCException, SQLException {
        updateListeners("Reading primary keys for " + database.toString() + " ...");

        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();

        for (Table table : tablesMap.values()) {
            ResultSet rs = databaseMetaData.getPrimaryKeys(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), table.getName());

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                short position = rs.getShort("KEY_SEQ");

                boolean foundExistingPK = false;
                for (PrimaryKey pk : foundPKs) {
                    if (pk.getTableName().equals(tableName)) {
                        pk.addColumnName(position - 1, columnName);

                        foundExistingPK = true;
                    }
                }

                if (!foundExistingPK) {
                    PrimaryKey primaryKey = new PrimaryKey();
                    primaryKey.setTableName(tableName);
                    primaryKey.addColumnName(position - 1, columnName);
                    primaryKey.setName(rs.getString("PK_NAME"));

                    foundPKs.add(primaryKey);
                }
            }

            rs.close();
        }

        this.primaryKeys.addAll(foundPKs);
    }

//    private void readUniqueConstraints(String catalog, String schema) throws JDBCException, SQLException {
//        updateListeners("Reading unique constraints for " + database.toString() + " ...");
//
//        //noinspection unchecked
//        List<String> sequenceNamess = (List<String>) new JdbcTemplate(database).queryForList(database.findUniqueConstraints(schema), String.class);
//
//        for (String sequenceName : sequenceNamess) {
//            Sequence seq = new Sequence();
//            seq.setName(sequenceName);
//
//            sequences.add(seq);
//        }
//    }

    private void readSequences(String schema) throws JDBCException, SQLException {
        updateListeners("Reading sequences for " + database.toString() + " ...");

        if (database.supportsSequences()) {
            //noinspection unchecked
            List<String> sequenceNamess = (List<String>) new JdbcTemplate(database).queryForList(database.createFindSequencesSQL(schema), String.class);

            for (String sequenceName : sequenceNamess) {
                Sequence seq = new Sequence();
                seq.setName(sequenceName);

                sequences.add(seq);
            }
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

    /**
     * Returns the table object for the given tableName.  If table does not exist, returns null
     */
    public Table getTable(String tableName) {
        for (Table table : getTables()) {
            if (table.getName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        return null;
    }

    public ForeignKey getForeignKey(String foreignKeyName) {
        for (ForeignKey fk : getForeignKeys()) {
            if (fk.getName().equalsIgnoreCase(foreignKeyName)) {
                return fk;
            }
        }
        return null;
    }
}
