package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.JdbcConnection;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.structure.*;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.SelectSequencesStatement;
import liquibase.util.StringUtils;

import java.sql.*;
import java.text.ParseException;
import java.util.*;

public abstract class JdbcDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {

    private Set<DiffStatusListener> statusListeners;

    public DatabaseSnapshot createSnapshot(Database database, String requestedSchema, Set<DiffStatusListener> listeners) throws DatabaseException {


        if (requestedSchema == null) {
            requestedSchema = database.getDefaultSchemaName();
        }

        try {

            DatabaseMetaData databaseMetaData = null;
            if (database.getConnection() != null) {
                databaseMetaData = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().getMetaData();
            }
            this.statusListeners = listeners;

            DatabaseSnapshot snapshot = new DatabaseSnapshot(database, requestedSchema);

            readTablesAndViews(snapshot, requestedSchema, databaseMetaData);
            readForeignKeyInformation(snapshot, requestedSchema, databaseMetaData);
            readPrimaryKeys(snapshot, requestedSchema, databaseMetaData);
            readColumns(snapshot, requestedSchema, databaseMetaData);
            readUniqueConstraints(snapshot, requestedSchema, databaseMetaData);
            readIndexes(snapshot, requestedSchema, databaseMetaData);
            readSequences(snapshot, requestedSchema, databaseMetaData);

            return snapshot;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }


    protected void readTablesAndViews(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading tables for " + database.toString() + " ...");
        // try to switch off auto detection of the liquibase's system table.
        if (database.isPeculiarLiquibaseSchema()) {
            ResultSet rs = databaseMetaData.getTables(database.convertRequestedSchemaToCatalog(database.getLiquibaseSchemaName()), database.convertRequestedSchemaToSchema(database.getLiquibaseSchemaName()), Database.databaseChangeLogTableName, new String[]{"TABLE"});
            while (rs.next()) {
                String name = convertFromDatabaseName(rs.getString("TABLE_NAME"));
                if (name.equalsIgnoreCase(database.getDatabaseChangeLogTableName())) {
                    snapshot.setDatabaseChangeLogTable(new Table(name).setSchema(database.getLiquibaseSchemaName()));
                }

                if (name.equalsIgnoreCase(database.getDatabaseChangeLogLockTableName())) {
                    snapshot.setDatabaseChangeLogLockTable(new Table(name).setSchema(database.getLiquibaseSchemaName()));
                }
            }
            rs.close();
        }

        ResultSet rs = databaseMetaData.getTables(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, new String[]{"TABLE", "VIEW", "ALIAS"});
        while (rs.next()) {
            String type = rs.getString("TABLE_TYPE");
            String name = convertFromDatabaseName(rs.getString("TABLE_NAME"));
            String schemaName = convertFromDatabaseName(rs.getString("TABLE_SCHEM"));
            String catalogName = convertFromDatabaseName(rs.getString("TABLE_CAT"));
            String remarks = rs.getString("REMARKS");

            if (database.isSystemTable(catalogName, schemaName, name) || database.isLiquibaseTable(name) || database.isSystemView(catalogName, schemaName, name)) {
                if (name.equalsIgnoreCase(database.getDatabaseChangeLogTableName()) && !database.isPeculiarLiquibaseSchema()) {
                    snapshot.setDatabaseChangeLogTable(new Table(name).setSchema(schemaName));
                }

                if (name.equalsIgnoreCase(database.getDatabaseChangeLogLockTableName())) {
                    snapshot.setDatabaseChangeLogLockTable(new Table(name).setSchema(schemaName));

                }
                continue;
            }

            if ("TABLE".equals(type) || "ALIAS".equals(type)) {
                Table table = new Table(name);
                table.setRemarks(StringUtils.trimToNull(remarks));
                table.setDatabase(database);
                table.setSchema(schemaName);
                snapshot.getTables().add(table);
            } else if ("VIEW".equals(type)) {
                View view = new View();
                view.setName(name);
                view.setSchema(schemaName);
                try {
                    view.setDefinition(database.getViewDefinition(schema, name));
                } catch (DatabaseException e) {
                    System.out.println("Error getting " + database.getConnection().getURL() + " view with " + new GetViewDefinitionStatement(schema, name));
                    throw e;
                }
                snapshot.getViews().add(view);
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

    protected void readColumns(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading columns for " + database.toString() + " ...");

        Statement selectStatement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
        ResultSet rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, null);
        while (rs.next()) {
            Column columnInfo = new Column();

            String tableName = convertFromDatabaseName(rs.getString("TABLE_NAME"));
            String columnName = convertFromDatabaseName(rs.getString("COLUMN_NAME"));
            String schemaName = convertFromDatabaseName(rs.getString("TABLE_SCHEM"));
            String catalogName = convertFromDatabaseName(rs.getString("TABLE_CAT"));
            String remarks = rs.getString("REMARKS");

            Table table;
            if (database.isSystemTable(catalogName, schemaName, tableName) || database.isLiquibaseTable(tableName)) {
                if (tableName.equalsIgnoreCase(database.getDatabaseChangeLogTableName())) {
                    table = snapshot.getDatabaseChangeLogTable();
                } else if (tableName.equalsIgnoreCase(database.getDatabaseChangeLogLockTableName())) {
                    table = snapshot.getDatabaseChangeLogLockTable();
                } else {
                    continue;
                }
            } else {
                table = snapshot.getTable(tableName);
            }
            if (table == null) {
                View view = snapshot.getView(tableName);
                if (view == null) {
                    LogFactory.getLogger().info("Could not find table or view " + tableName + " for column " + columnName);
                    continue;
                } else {
                    columnInfo.setView(view);
                    columnInfo.setAutoIncrement(false);
                    view.getColumns().add(columnInfo);
                }
            } else {
                columnInfo.setTable(table);
                columnInfo.setAutoIncrement(isColumnAutoIncrement(database, schema, tableName, columnName));
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

            columnInfo.setPrimaryKey(snapshot.isPrimaryKey(columnInfo));

            getColumnTypeAndDefValue(columnInfo, rs, database);
            columnInfo.setRemarks(remarks);
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
    protected void getColumnTypeAndDefValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, DatabaseException {
        Object defaultValue = rs.getObject("COLUMN_DEF");
        try {
            columnInfo.setDefaultValue(TypeConverterFactory.getInstance().findTypeConverter(database).convertDatabaseValueToObject(defaultValue, columnInfo.getDataType(), columnInfo.getColumnSize(), columnInfo.getDecimalDigits(), database));
        } catch (ParseException e) {
            throw new DatabaseException(e);
        }
        columnInfo.setTypeName(TypeConverterFactory.getInstance().findTypeConverter(database).getColumnType(rs.getString("TYPE_NAME"), columnInfo.isAutoIncrement()));

    } // end of method getColumnTypeAndDefValue()

    protected void readForeignKeyInformation(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading foreign keys for " + database.toString() + " ...");

        for (Table table : snapshot.getTables()) {
            String dbCatalog = database.convertRequestedSchemaToCatalog(schema);
            String dbSchema = database.convertRequestedSchemaToSchema(schema);
            ResultSet rs = databaseMetaData.getExportedKeys(dbCatalog, dbSchema, table.getName());
            ForeignKey fkInfo = null;
            while (rs.next()) {
                String fkName = convertFromDatabaseName(rs.getString("FK_NAME"));

                String pkTableName = convertFromDatabaseName(rs.getString("PKTABLE_NAME"));
                String pkColumn = convertFromDatabaseName(rs.getString("PKCOLUMN_NAME"));
                Table pkTable = snapshot.getTable(pkTableName);
                if (pkTable == null) {
                    //Ok, no idea what to do with this one . . . should always be there
                    LogFactory.getLogger().warning("Foreign key " + fkName + " references table " + pkTableName + ", which we cannot find.  Ignoring.");
                    continue;
                }
                int keySeq = rs.getInt("KEY_SEQ");
                //Simple (non-composite) keys have KEY_SEQ=1, so create the ForeignKey.
                //In case of subsequent parts of composite keys (KEY_SEQ>1) don't create new instance, just reuse the one from previous call.
                //According to #getExportedKeys() contract, the result set rows are properly sorted, so the reuse of previous FK instance is safe.
//                if (keySeq == 1) {
                    fkInfo = new ForeignKey();
//                }

//                if (fkInfo == null || ( (fkInfo.getPrimaryKeyTable() != null) && (!fkInfo.getPrimaryKeyTable().getName().equals(pkTableName)))) {
//                    fkInfo = new ForeignKey();
//                }

                fkInfo.setPrimaryKeyTable(pkTable);
                fkInfo.addPrimaryKeyColumn(pkColumn);

                String fkTableName = convertFromDatabaseName(rs.getString("FKTABLE_NAME"));
                String fkSchema = convertFromDatabaseName(rs.getString("FKTABLE_SCHEM"));
                String fkColumn = convertFromDatabaseName(rs.getString("FKCOLUMN_NAME"));
                Table fkTable = snapshot.getTable(fkTableName);
                if (fkTable == null) {
                    fkTable = new Table(fkTableName);
                    fkTable.setDatabase(database);
                    fkTable.setSchema(fkSchema);
                    LogFactory.getLogger().warning("Foreign key " + fkName + " is in table " + fkTableName + ", which is in a different schema.  Retaining FK in diff, but table will not be diffed.");
                }
                fkInfo.setForeignKeyTable(fkTable);
                fkInfo.addForeignKeyColumn(fkColumn);

                fkInfo.setName(fkName);

                ForeignKeyConstraintType updateRule, deleteRule;
                updateRule = convertToForeignKeyConstraintType(rs.getInt("UPDATE_RULE"));
                if (rs.wasNull())
                    updateRule = null;
                deleteRule = convertToForeignKeyConstraintType(rs.getInt("DELETE_RULE"));
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
                    snapshot.getForeignKeys().add(fkInfo);
                }
            }

            rs.close();
        }
    }

    protected ForeignKeyConstraintType convertToForeignKeyConstraintType(int jdbcType) throws DatabaseException {
        if (jdbcType == DatabaseMetaData.importedKeyCascade) {
            return ForeignKeyConstraintType.importedKeyCascade;
        } else if (jdbcType == DatabaseMetaData.importedKeyNoAction) {
            return ForeignKeyConstraintType.importedKeyNoAction;
        } else if (jdbcType == DatabaseMetaData.importedKeyRestrict) {
            return ForeignKeyConstraintType.importedKeyRestrict;
        } else if (jdbcType == DatabaseMetaData.importedKeySetDefault) {
            return ForeignKeyConstraintType.importedKeySetDefault;
        } else if (jdbcType == DatabaseMetaData.importedKeySetNull) {
            return ForeignKeyConstraintType.importedKeySetNull;
        } else {
            throw new DatabaseException("Unknown constraint type: "+jdbcType);
        }
    }

    protected void readIndexes(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading indexes for " + database.toString() + " ...");

        for (Table table : snapshot.getTables()) {
            ResultSet rs;
            Statement statement = null;
            if (database instanceof OracleDatabase) {
                //oracle getIndexInfo is buggy and slow.  See Issue 1824548 and http://forums.oracle.com/forums/thread.jspa?messageID=578383&#578383  
                statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
                String sql = "SELECT INDEX_NAME, 3 AS TYPE, TABLE_NAME, COLUMN_NAME, COLUMN_POSITION AS ORDINAL_POSITION, null AS FILTER_CONDITION FROM ALL_IND_COLUMNS WHERE TABLE_OWNER='" + database.convertRequestedSchemaToSchema(schema) + "' AND TABLE_NAME='" + table.getName() + "' ORDER BY INDEX_NAME, ORDINAL_POSITION";
                rs = statement.executeQuery(sql);
            } else {
                rs = databaseMetaData.getIndexInfo(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), table.getName(), false, true);
            }
            Map<String, Index> indexMap = new HashMap<String, Index>();
            while (rs.next()) {
                String indexName = convertFromDatabaseName(rs.getString("INDEX_NAME"));
                /*
                 * TODO Informix generates indexnames with a leading blank if no name given.
                 * An identifier with a leading blank is not allowed.
                 * So here is it replaced.
                 */
                if (database instanceof InformixDatabase && indexName.startsWith(" ")) {
                    indexName = "_generated_index_" + indexName.substring(1);
                }
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
                /*
                 * TODO maybe bug in jdbc driver? Need to investigate.
                 * If this "if" is commented out ArrayOutOfBoundsException is thrown
                 * because it tries to access an element -1 of a List (position-1)
                 */
                if (database instanceof InformixDatabase
                        && type != DatabaseMetaData.tableIndexStatistic
                        && position == 0) {
                    System.out.println(this.getClass().getName() + ": corrected position to " + ++position);
                }
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
                indexInformation.getColumns().set(position - 1, columnName);
            }
            for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
                snapshot.getIndexes().add(entry.getValue());
            }
            rs.close();
            if (statement != null) {
                statement.close();
            }
        }

        Set<Index> indexesToRemove = new HashSet<Index>();
        //remove PK indexes
        for (Index index : snapshot.getIndexes()) {
            for (PrimaryKey pk : snapshot.getPrimaryKeys()) {
                if (index.getTable().getName().equalsIgnoreCase(pk.getTable().getName()) && index.getColumnNames().equals(pk.getColumnNames())) {
                    indexesToRemove.add(index);
                }
            }
            for (ForeignKey fk : snapshot.getForeignKeys()) {
                if (index.getTable().getName().equalsIgnoreCase(fk.getForeignKeyTable().getName()) && index.getColumnNames().equals(fk.getForeignKeyColumns())) {
                    indexesToRemove.add(index);
                }
            }
            for (UniqueConstraint uc : snapshot.getUniqueConstraints()) {
                if (index.getTable().getName().equalsIgnoreCase(uc.getTable().getName()) && index.getColumnNames().equals(uc.getColumnNames())) {
                    indexesToRemove.add(index);
                }
            }

        }
        snapshot.getIndexes().removeAll(indexesToRemove);
    }

    protected void readPrimaryKeys(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading primary keys for " + database.toString() + " ...");

        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();

        for (Table table : snapshot.getTables()) {
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

        snapshot.getPrimaryKeys().addAll(foundPKs);
    }

    protected String convertPrimaryKeyName(String pkName) throws SQLException {
        return pkName;
    }

    protected void readUniqueConstraints(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading unique constraints for " + database.toString() + " ...");
    }

//    private void readUniqueConstraints(String catalog, String schema) throws DatabaseException, SQLException {
//        updateListeners("Reading unique constraints for " + database.toString() + " ...");
//
//        //noinspection unchecked
//        List<String> sequenceNamess = (List<String>) new Executor(database).queryForList(database.findUniqueConstraints(schema), String.class);
//
//        for (String sequenceName : sequenceNamess) {
//            Sequence seq = new Sequence();
//            seq.setName(sequenceName);
//
//            sequences.add(seq);
//        }
//    }

    protected void readSequences(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading sequences for " + database.toString() + " ...");

        String convertedSchemaName = database.convertRequestedSchemaToSchema(schema);

        if (database.supportsSequences()) {
            //noinspection unchecked
            List<String> sequenceNames = (List<String>) ExecutorService.getInstance().getExecutor(database).queryForList(new SelectSequencesStatement(schema), String.class, new ArrayList<SqlVisitor>());


            if (sequenceNames != null) {
                for (String sequenceName : sequenceNames) {
                    Sequence seq = new Sequence();
                    seq.setName(sequenceName.trim());
                    seq.setSchema(convertedSchemaName);

                    snapshot.getSequences().add(seq);
                }
            }
        }
    }

    protected void updateListeners(String message) {
        if (this.statusListeners == null) {
            return;
        }
        LogFactory.getLogger().debug(message);
        for (DiffStatusListener listener : this.statusListeners) {
            listener.statusUpdate(message);
        }
    }

    public boolean isColumnAutoIncrement(Database database, String schemaName, String tableName, String columnName) throws SQLException, DatabaseException {
        if (!database.supportsAutoIncrement()) {
            return false;
        }

        boolean autoIncrement = false;

        ResultSet selectRS = null;
        try {
            selectRS = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().executeQuery("SELECT " + database.escapeColumnName(schemaName, tableName, columnName) + " FROM " + database.escapeTableName(schemaName, tableName) + " WHERE 1 = 0");
            ResultSetMetaData meta = selectRS.getMetaData();
            autoIncrement = meta.isAutoIncrement(1);
        } finally {
            if (selectRS != null) {
                selectRS.close();
            }
        }

        return autoIncrement;
    }

    public int getDatabaseType(int type, Database database) {
        int returnType = type;
        if (returnType == java.sql.Types.BOOLEAN) {
            String booleanType = TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getDataTypeName();
            if (!booleanType.equalsIgnoreCase("boolean")) {
                returnType = java.sql.Types.TINYINT;
            }
        }

        return returnType;
    }

}
