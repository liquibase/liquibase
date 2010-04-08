package liquibase.database.structure;

import liquibase.database.AbstractDatabase;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.sql.visitor.SqlVisitor;
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

public abstract class SqlDatabaseSnapshot implements DatabaseSnapshot {

    protected DatabaseMetaData databaseMetaData;
    protected Database database;

    protected Set<Table> tables = new HashSet<Table>();
    protected Set<View> views = new HashSet<View>();
    protected Set<Column> columns = new HashSet<Column>();
    protected Set<ForeignKey> foreignKeys = new HashSet<ForeignKey>();
    protected Set<UniqueConstraint> uniqueConstraints = new HashSet<UniqueConstraint>();
    protected Set<Index> indexes = new HashSet<Index>();
    protected Set<PrimaryKey> primaryKeys = new HashSet<PrimaryKey>();
    protected Set<Sequence> sequences = new HashSet<Sequence>();


    protected Map<String, Table> tablesMap = new HashMap<String, Table>();
    protected Map<String, View> viewsMap = new HashMap<String, View>();
    protected Map<String, Column> columnsMap = new HashMap<String, Column>();

    private Set<DiffStatusListener> statusListeners;

    protected static final Logger log = LogFactory.getLogger();
    private String schema;

    private boolean hasDatabaseChangeLogTable = false;


    /**
     * Creates an empty database snapshot
     */
    public SqlDatabaseSnapshot() {
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public SqlDatabaseSnapshot(Database database) throws JDBCException {
        this(database, null, null);
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public SqlDatabaseSnapshot(Database database, String schema) throws JDBCException {
        this(database, null, schema);
    }

    /**
     * Creates a snapshot of the given database.
     */
    public SqlDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        this(database, statusListeners, database.getDefaultSchemaName());
    }

    /**
     * Creates a snapshot of the given database.
     */
    public SqlDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        if (requestedSchema == null) {
            requestedSchema = database.getDefaultSchemaName();
        }

        try {
            this.schema = requestedSchema;
            this.database = database;
            this.databaseMetaData = database.getConnection().getMetaData();
            this.statusListeners = statusListeners;

            log.finest("Reading table and views ....");
            readTablesAndViews(requestedSchema);
            log.finest("Reading foreign keys ....");
            readForeignKeyInformation(requestedSchema);
            log.finest("Reading primary keys ....");
            readPrimaryKeys(requestedSchema);
            log.finest("Reading columns ....");
            readColumns(requestedSchema);
            log.finest("Reading unique constraints ....");
            readUniqueConstraints(requestedSchema);
            log.finest("Reading indexes ....");
            readIndexes(requestedSchema);
            log.finest("Reading sequences ....");
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
            return getColumn(column.getView().getName(), column.getName());
        } else {
            return getColumn(column.getTable().getName(), column.getName());
        }
    }

    public Column getColumn(String tableName, String columnName) {
        String tableAndColumn = tableName + "." + columnName;
        Column returnColumn = columnsMap.get(tableAndColumn);
        if (returnColumn == null) {
            for (String key : columnsMap.keySet()) {
                if (key.equalsIgnoreCase(tableAndColumn)) {
                    return columnsMap.get(key);
                }
            }
        }
        return returnColumn;
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

    public Set<UniqueConstraint> getUniqueConstraints() {
        return this.uniqueConstraints;
    }

    protected void readTablesAndViews(String schema) throws SQLException, JDBCException {
        updateListeners("Reading tables for " + database.toString() + " ...");
        ResultSet rs = databaseMetaData.getTables(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, new String[]{"TABLE", "VIEW", "ALIAS"});
        while (rs.next()) {
            String type = rs.getString("TABLE_TYPE");
            String name = convertFromDatabaseName(rs.getString("TABLE_NAME"));
            String schemaName = convertFromDatabaseName(rs.getString("TABLE_SCHEM"));
            String catalogName = convertFromDatabaseName(rs.getString("TABLE_CAT"));
            String remarks = rs.getString("REMARKS");

            if (database.isSystemTable(catalogName, schemaName, name) || database.isLiquibaseTable(name) || database.isSystemView(catalogName, schemaName, name)) {
                if (name.equalsIgnoreCase(database.getDatabaseChangeLogTableName())) {
                    hasDatabaseChangeLogTable = true;
                }
                continue;
            }

            if ("TABLE".equals(type) || "ALIAS".equals(type)) {
                Table table = new Table(name);
                table.setRemarks(StringUtils.trimToNull(remarks));
                table.setDatabase(database);
                table.setSchema(schemaName);
                tablesMap.put(name, table);
            } else if ("VIEW".equals(type)) {
                View view = new View();
                view.setName(name);
                view.setSchema(schemaName);
                try {
                    view.setDefinition(database.getViewDefinition(schema, name));
                } catch (JDBCException e) {
                    System.out.println("Error getting " + database.getConnectionURL() + " view with " + ((AbstractDatabase) database).getViewDefinitionSql(schema, name));
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

    protected String convertFromDatabaseName(String objectName) {
        if (objectName == null) {
            return null;
        }
        return objectName;
    }

    protected void readColumns(String schema) throws SQLException, JDBCException {
        updateListeners("Reading columns for " + database.toString() + " ...");

        Statement selectStatement = database.getConnection().createStatement();
        ResultSet rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, null);
        while (rs.next()) {
            Column columnInfo = new Column();

            String tableName = convertFromDatabaseName(rs.getString("TABLE_NAME"));
            String columnName = convertFromDatabaseName(rs.getString("COLUMN_NAME"));
            String schemaName = convertFromDatabaseName(rs.getString("TABLE_SCHEM"));
            String catalogName = convertFromDatabaseName(rs.getString("TABLE_CAT"));
            String remarks = rs.getString("REMARKS");

            if (database.isSystemTable(catalogName, schemaName, tableName) || database.isLiquibaseTable(tableName)) {
                continue;
            }

            Table table = tablesMap.get(tableName);
            if (table == null) {
                View view = viewsMap.get(tableName);
                if (view == null) {
                    // Not a table or view column. It's probably an index or primary key column, so ignore it.
                    continue;
                } else {
                    columnInfo.setView(view);
                    columnInfo.setAutoIncrement(false);
                    view.getColumns().add(columnInfo);
                }
            } else {
                columnInfo.setTable(table);
                columnInfo.setAutoIncrement(database.isColumnAutoIncrement(schemaName, tableName, columnName));
                table.getColumns().add(columnInfo);
            }

            columnInfo.setName(columnName);
            columnInfo.setDataType(rs.getInt("DATA_TYPE"));
            columnInfo.setColumnSize(rs.getInt("COLUMN_SIZE"));
            columnInfo.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));

            int nullable = rs.getInt("NULLABLE");
            if (nullable == DatabaseMetaData.columnNoNulls) {
                columnInfo.setNullable(false);
            } else if (nullable == DatabaseMetaData.columnNullable) {
                columnInfo.setNullable(true);
            }

            columnInfo.setPrimaryKey(isPrimaryKey(columnInfo));

            getColumnTypeAndDefValue(columnInfo, rs, database);
            columnInfo.setRemarks(remarks);
            columnsMap.put(tableName + "." + columnName, columnInfo);
        }
        rs.close();
        selectStatement.close();
    }

    /**
     * Method assigns correct column type and default value to Column object.
     * <p/>
     * This method should be database engine specific. JDBC implementation requires
     * database engine vendors to convert native DB types to java objects.
     * During conversion some metadata information are being lost or reported incorrectly via DatabaseMetaData objects.
     * This method, if necessary, must be overriden. It must go below DatabaseMetaData implementation and talk directly to database to get correct metadata information.
     *
     * @param rs
     * @return void
     * @throws SQLException
     * @author Daniel Bargiel <danielbargiel@googlemail.com>
     */
    protected void getColumnTypeAndDefValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, JDBCException {
        Object defaultValue = rs.getObject("COLUMN_DEF");
        try {
            columnInfo.setDefaultValue(database.convertDatabaseValueToJavaObject(defaultValue, columnInfo.getDataType(), columnInfo.getColumnSize(), columnInfo.getDecimalDigits()));
        } catch (ParseException e) {
            throw new JDBCException(e);
        }
        columnInfo.setTypeName(database.getColumnType(rs.getString("TYPE_NAME"), columnInfo.isAutoIncrement()));

    } // end of method getColumnTypeAndDefValue()

    protected boolean isPrimaryKey(Column columnInfo) {
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

    protected void readForeignKeyInformation(String schema) throws JDBCException, SQLException {
        updateListeners("Reading foreign keys for " + database.toString() + " ...");

        for (Table table : tablesMap.values()) {
            String dbCatalog = database.convertRequestedSchemaToCatalog(schema);
            String dbSchema = database.convertRequestedSchemaToSchema(schema);
            ResultSet rs = databaseMetaData.getExportedKeys(dbCatalog, dbSchema, table.getName());
            ForeignKey fkInfo = null;
            while (rs.next()) {
                String fkName = convertFromDatabaseName(rs.getString("FK_NAME"));

                String pkTableName = convertFromDatabaseName(rs.getString("PKTABLE_NAME"));
                String pkColumn = convertFromDatabaseName(rs.getString("PKCOLUMN_NAME"));
                Table pkTable = tablesMap.get(pkTableName);
                if (pkTable == null) {
                    //Ok, no idea what to do with this one . . . should always be there
                    log.warning("Foreign key " + fkName + " references table " + pkTableName + ", which we cannot find.  Ignoring.");
                    continue;
                }
                int keySeq = rs.getInt("KEY_SEQ");
                //Simple (non-composite) keys have KEY_SEQ=1, so create the ForeignKey.
                //In case of subsequent parts of composite keys (KEY_SEQ>1) don't create new instance, just reuse the one from previous call.
                //According to #getExportedKeys() contract, the result set rows are properly sorted, so the reuse of previous FK instance is safe.
        /*        if (keySeq == 1) {
                    fkInfo = new ForeignKey();
                } */

                if (fkInfo == null || ( (fkInfo != null) && (!fkInfo.getPrimaryKeyTable().getName().equals(pkTableName)))) {
                    fkInfo = new ForeignKey();
                }

                fkInfo.setPrimaryKeyTable(pkTable);
                fkInfo.addPrimaryKeyColumn(pkColumn);

                String fkTableName = convertFromDatabaseName(rs.getString("FKTABLE_NAME"));
                String fkSchema = convertFromDatabaseName(rs.getString("FKTABLE_SCHEM"));
                String fkColumn = convertFromDatabaseName(rs.getString("FKCOLUMN_NAME"));
                Table fkTable = tablesMap.get(fkTableName);
                if (fkTable == null) {
                    fkTable = new Table(fkTableName);
                    fkTable.setDatabase(database);
                    fkTable.setSchema(fkSchema);
                    log.warning("Foreign key " + fkName + " is in table " + fkTableName + ", which is in a different schema.  Retaining FK in diff, but table will not be diffed.");
                }
                fkInfo.setForeignKeyTable(fkTable);
                fkInfo.addForeignKeyColumn(fkColumn);

                fkInfo.setName(fkName);

                Integer updateRule, deleteRule;
                updateRule = rs.getInt("UPDATE_RULE");
                if (rs.wasNull())
                    updateRule = null;
                deleteRule = rs.getInt("DELETE_RULE");
                if (rs.wasNull()) {
                    deleteRule = null;
                }
                fkInfo.setUpdateRule(updateRule);
                fkInfo.setDeleteRule(deleteRule);

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

    protected void readIndexes(String schema) throws JDBCException, SQLException {
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
                String indexName = convertFromDatabaseName(rs.getString("INDEX_NAME"));
                short type = rs.getShort("TYPE");
//                String tableName = rs.getString("TABLE_NAME");
                boolean nonUnique = true;
                try {
                    nonUnique = rs.getBoolean("NON_UNIQUE");
                } catch (SQLException e) {
                    //doesn't exist in all databases
                }
                String columnName = convertFromDatabaseName(rs.getString("COLUMN_NAME"));
                short position = rs.getShort("ORDINAL_POSITION");
                String filterCondition = rs.getString("FILTER_CONDITION");

                if (type == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }
//                if (type == DatabaseMetaData.tableIndexOther) {
//                    continue;
//                }

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
                    indexInformation.setUnique(!nonUnique);
                    indexInformation.setFilterCondition(filterCondition);
                    indexMap.put(indexName, indexInformation);
                }

                for (int i = indexInformation.getColumns().size(); i < position; i++) {
                    indexInformation.getColumns().add(null);
                }
                indexInformation.getColumns().set(position-1, columnName);
            }
            for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
                indexes.add(entry.getValue());
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
                if (index.getTable().getName().equalsIgnoreCase(pk.getTable().getName()) && index.getColumnNames().equals(pk.getColumnNames())) {
                    indexesToRemove.add(index);
                }
            }
            for (ForeignKey fk : foreignKeys) {
                if (index.getTable().getName().equalsIgnoreCase(fk.getForeignKeyTable().getName()) && index.getColumnNames().equals(fk.getForeignKeyColumns())) {
                    indexesToRemove.add(index);
                }
            }
            for (UniqueConstraint uc : uniqueConstraints) {
                if (index.getTable().getName().equalsIgnoreCase(uc.getTable().getName()) && index.getColumnNames().equals(uc.getColumnNames())) {
                    indexesToRemove.add(index);
                }
            }

        }
        indexes.removeAll(indexesToRemove);
    }

    protected void readPrimaryKeys(String schema) throws JDBCException, SQLException {
        updateListeners("Reading primary keys for " + database.toString() + " ...");

        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();

        for (Table table : tablesMap.values()) {
            ResultSet rs = databaseMetaData.getPrimaryKeys(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), table.getName());

            while (rs.next()) {
                String tableName = convertFromDatabaseName(rs.getString("TABLE_NAME"));
                String columnName = convertFromDatabaseName(rs.getString("COLUMN_NAME"));
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
                    primaryKey.setName(convertPrimaryKeyName(rs.getString("PK_NAME")));

                    foundPKs.add(primaryKey);
                }
            }

            rs.close();
        }

        this.primaryKeys.addAll(foundPKs);
    }

    protected String convertPrimaryKeyName(String pkName) throws SQLException {
        return pkName;
    }

    protected void readUniqueConstraints(String schema) throws JDBCException, SQLException {
        updateListeners("Reading unique constraints for " + database.toString() + " ...");
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

    protected void readSequences(String schema) throws JDBCException {
        updateListeners("Reading sequences for " + database.toString() + " ...");

        String convertedSchemaName = database.convertRequestedSchemaToSchema(schema);

        if (database.supportsSequences()) {
            //noinspection unchecked
            List<String> sequenceNames = (List<String>) database.getJdbcTemplate().queryForList(database.createFindSequencesSQL(schema), String.class, new ArrayList<SqlVisitor>());


            if (sequenceNames != null) {
                for (String sequenceName : sequenceNames) {
                    Sequence seq = new Sequence();
                    seq.setName(sequenceName.trim());
                    seq.setSchema(convertedSchemaName);

                    sequences.add(seq);
                }
            }
        }
    }

    protected void updateListeners(String message) {
        if (this.statusListeners == null) {
            return;
        }
        log.finest(message);
        for (DiffStatusListener listener : this.statusListeners) {
            listener.statusUpdate(message);
        }
    }

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

    public PrimaryKey getPrimaryKeyForTable(String tableName) {
        for (PrimaryKey pk : getPrimaryKeys()) {
            if (pk.getTable().getName().equalsIgnoreCase(tableName)) {
                return pk;
            }
        }
        return null;
    }

    public UniqueConstraint getUniqueConstraint(String ucName) {
        for (UniqueConstraint uc : getUniqueConstraints()) {
            if (uc.getName().equalsIgnoreCase(ucName)) {
                return uc;
            }
        }
        return null;
    }

    public String getSchema() {
        return schema;
    }

    public boolean hasDatabaseChangeLogTable() {
        return hasDatabaseChangeLogTable;
    }
}
