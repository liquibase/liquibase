package liquibase.database.structure;

import liquibase.database.AbstractDatabase;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;
import liquibase.log.LogFactory;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
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

    private static final Logger log = LogFactory.getLogger();


    /**
     * Creates an empty database snapshot
     */
    public DatabaseSnapshot() {
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public DatabaseSnapshot(Database database) throws JDBCException {
        this(database, null, null);
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public DatabaseSnapshot(Database database, String schema) throws JDBCException {
        this(database, null, schema);
    }

    /**
     * Creates a snapshot of the given database.
     */
    public DatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        this(database, statusListeners, database.getDefaultSchemaName());
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
            String remarks = rs.getString("REMARKS");

            if (database.isSystemTable(catalogName, schemaName, name) || database.isLiquibaseTable(name) || database.isSystemView(catalogName, schemaName, name)) {
                continue;
            }

            if ("TABLE".equals(type)) {
                Table table = new Table(name);
                table.setRemarks(StringUtils.trimToNull(remarks));
                table.setDatabase(database);
                tablesMap.put(name, table);
            } else if ("VIEW".equals(type)) {
                View view = new View();
                view.setName(name);
                try {
                    view.setDefinition(database.getViewDefinition(schema, name));
                } catch (JDBCException e) {
                    System.out.println("Error getting view with " + ((AbstractDatabase) database).getViewDefinitionSql(schema, name));
                    throw e;
                }

                viewsMap.put(name, view);

            }
        }
        rs.close();

        /* useful for troubleshooting table reading */
//        if (tablesMap.size() == 0) {
//            System.out.println("No tables found, all tables:");
//
//            String convertedCatalog = database.convertRequestedSchemaToCatalog(schema);
//            String convertedSchema = database.convertRequestedSchemaToSchema(schema);
//
//            System.out.println("Tried: "+convertedCatalog+"."+convertedSchema);
//            convertedCatalog = null;
//            convertedSchema = null;
//
//            rs = databaseMetaData.getTables(convertedCatalog, convertedSchema, null, new String[]{"TABLE", "VIEW"});
//            while (rs.next()) {
//                String type = rs.getString("TABLE_TYPE");
//                String name = rs.getString("TABLE_NAME");
//                String schemaName = rs.getString("TABLE_SCHEM");
//                String catalogName = rs.getString("TABLE_CAT");
//
//                if (database.isSystemTable(catalogName, schemaName, name) || database.isLiquibaseTable(name) || database.isSystemView(catalogName, schemaName, name)) {
//                    continue;
//                }
//
//                System.out.println(catalogName+"."+schemaName+"."+name+":"+type);
//
//            }
//            rs.close();
//        }
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

            columnInfo.setAutoIncrement(database.isColumnAutoIncrement(schema, tableName, columnName));

            columnInfo.setTypeName(database.getColumnType(rs.getString("TYPE_NAME"), columnInfo.isAutoIncrement()));            


            columnsMap.put(tableName + "." + columnName, columnInfo);
        }
        rs.close();
        selectStatement.close();
    }

    private boolean isPrimaryKey(Column columnInfo) {
        for (PrimaryKey pk : getPrimaryKeys()) {
            if (columnInfo.getTable() == null) {
                continue;
            }
            if (pk.getTable().getName().equalsIgnoreCase(columnInfo.getTable().getName())) {
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
            String dbCatalog = database.convertRequestedSchemaToCatalog(schema);
            String dbSchema = database.convertRequestedSchemaToSchema(schema);
            ResultSet rs = databaseMetaData.getExportedKeys(dbCatalog, dbSchema, table.getName());
            ForeignKey fkInfo = null;
            while (rs.next()) {
                String pkTableName = rs.getString("PKTABLE_NAME");
                String pkColumn = rs.getString("PKCOLUMN_NAME");
                Table pkTable = tablesMap.get(pkTableName);
                if (pkTable == null) {
                    throw new JDBCException("Could not find table " + pkTableName + " for column " + pkColumn);
                }
                int keySeq = rs.getInt("KEY_SEQ");
                //Simple (non-composite) keys have KEY_SEQ=1, so create the ForeignKey.
                //In case of subsequent parts of composite keys (KEY_SEQ>1) don't create new instance, just reuse the one from previous call.
                //According to #getExportedKeys() contract, the result set rows are properly sorted, so the reuse of previous FK instance is safe.
                if (keySeq == 1) {
                	fkInfo = new ForeignKey();
                }
                
                fkInfo.setPrimaryKeyTable(pkTable);
                fkInfo.addPrimaryKeyColumn(pkColumn);

                String fkTableName = rs.getString("FKTABLE_NAME");
                String fkColumn = rs.getString("FKCOLUMN_NAME");
                Table fkTable = tablesMap.get(fkTableName);
                if (fkTable == null) {
                    throw new JDBCException("Could not find table " + fkTableName + " for column " + fkColumn);
                }
                fkInfo.setForeignKeyTable(fkTable);
                fkInfo.addForeignKeyColumn(fkColumn);

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

                //Add only if the key was created in this iteration (updating the instance values changes hashCode so it cannot be re-inserted into set) 
                if (keySeq == 1) {
                	foreignKeys.add(fkInfo);
                }
            }

            rs.close();
        }
    }

    private void readIndexes(String schema) throws JDBCException, SQLException {
        updateListeners("Reading indexes for " + database.toString() + " ...");

        for (Table table : tablesMap.values()) {
            ResultSet rs;
            Statement statement = null;
            if (database instanceof OracleDatabase) {
                //oracle getIndexInfo is buggy and slow.  See Issue 1824548 and http://forums.oracle.com/forums/thread.jspa?messageID=578383&#578383  
                statement = database.getConnection().createStatement();
                String sql = "SELECT INDEX_NAME, 3 AS TYPE, TABLE_NAME, COLUMN_NAME, COLUMN_POSITION AS ORDINAL_POSITION, null AS FILTER_CONDITION FROM ALL_IND_COLUMNS WHERE TABLE_OWNER='" + database.convertRequestedSchemaToSchema(schema) + "' AND TABLE_NAME='" + table.getName() + "' ORDER BY INDEX_NAME, ORDINAL_POSITION";
                rs = statement.executeQuery(sql);
            } else {
                rs = databaseMetaData.getIndexInfo(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), table.getName(), false, true);
            }
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
                    indexInformation.setTable(table);
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
            if (statement != null) {
                statement.close();
            }
        }

        Set<Index> indexesToRemove = new HashSet<Index>();
        //remove PK indexes
        for (Index index : indexes) {
            for (PrimaryKey pk : primaryKeys) {
                if (index.getTable().getName().equalsIgnoreCase(pk.getTable().getName())
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
                    if (pk.getTable().getName().equals(tableName)) {
                        pk.addColumnName(position - 1, columnName);

                        foundExistingPK = true;
                    }
                }

                if (!foundExistingPK) {
                    PrimaryKey primaryKey = new PrimaryKey();
                    primaryKey.setTable(table);
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
            List<String> sequenceNamess = (List<String>) database.getJdbcTemplate().queryForList(database.createFindSequencesSQL(schema), String.class);


            for (String sequenceName : sequenceNamess) {
                Sequence seq = new Sequence();
                seq.setName(sequenceName.trim());

                sequences.add(seq);
            }
        }
    }

    private void updateListeners(String message) {
        if (this.statusListeners == null) {
            return;
        }
        log.finest(message);
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

    public Sequence getSequence(String sequenceName) {
        for (Sequence sequence : getSequences()) {
            if (sequence.getName().equalsIgnoreCase(sequenceName)) {
                return sequence;
            }
        }
        return null;
    }

    public Index getIndex(String indexName) {
        for (Index index : getIndexes()) {
            if (index.getName().equalsIgnoreCase(indexName)) {
                return index;
            }
        }
        return null;
    }

    public View getView(String viewName) {
        for (View view : getViews()) {
            if (view.getName().equalsIgnoreCase(viewName)) {
                return view;
            }
        }
        return null;
    }

    public PrimaryKey getPrimaryKey(String pkName) {
        for (PrimaryKey pk : getPrimaryKeys()) {
            if (pk.getName().equalsIgnoreCase(pkName)) {
                return pk;
            }
        }
        return null;
    }
}
