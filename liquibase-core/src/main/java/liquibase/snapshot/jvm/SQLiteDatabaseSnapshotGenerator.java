package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.datatype.DataTypeFactory;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.structure.*;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.SelectSequencesStatement;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class SQLiteDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {

//    /**
//     * Creates an empty database snapshot
//     */
//    public SQLiteDatabaseSnapshotGenerator() {
//    }
//
    public boolean supports(Database database) {
        return database instanceof SQLiteDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }
//
//    @Override
//    protected void readTables(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
//
//        Database database = snapshot.getDatabase();
//
//        updateListeners("Reading tables for " + database.toString() + " ...");
//        ResultSet rs = databaseMetaData.getTables(
//                database.convertRequestedSchemaToCatalog(schema),
//                database.convertRequestedSchemaToSchema(schema),
//                null,
//                new String[]{"TABLE", "VIEW"});
//
//        try {
//            while (rs.next()) {
//                String type = rs.getString("TABLE_TYPE");
//                String name = rs.getString("TABLE_NAME");
//                String schemaName = rs.getString("TABLE_SCHEM");
//                String catalogName = rs.getString("TABLE_CAT");
//                String remarks = rs.getString("REMARKS");
//
//                if (database.isSystemTable(catalogName, schemaName, name) ||
//                        database.isLiquibaseTable(name) ||
//                        database.isSystemView(catalogName, schemaName, name)) {
//                    continue;
//                }
//
//                if ("TABLE".equals(type)) {
//                    Table table = new Table(name);
//                    table.setRemarks(StringUtils.trimToNull(remarks));
//                    table.setDatabase(database);
//                    table.setSchema(schemaName);
//                    snapshot.getTables().add(table);
//                } else if ("VIEW".equals(type)) {
//                    View view = new View(name);
//                    view.setSchema(schemaName);
//                    try {
//                        view.setDefinition(database.
//                                getViewDefinition(schema, name));
//                    } catch (DatabaseException e) {
//                        System.out.println("Error getting view with " + new GetViewDefinitionStatement(schema, name));
//                        throw e;
//                    }
//                    snapshot.getViews().add(view);
//                }
//            }
//        } finally {
//            rs.close();
//        }
//    }
//
//    @Override
//    protected void readViews(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
//
//        Database database = snapshot.getDatabase();
//
//        updateListeners("Reading tables for " + database.toString() + " ...");
//        ResultSet rs = databaseMetaData.getTables(
//                database.convertRequestedSchemaToCatalog(schema),
//                database.convertRequestedSchemaToSchema(schema),
//                null,
//                new String[]{"TABLE", "VIEW"});
//
//        try {
//            while (rs.next()) {
//                String type = rs.getString("TABLE_TYPE");
//                String name = rs.getString("TABLE_NAME");
//                String schemaName = rs.getString("TABLE_SCHEM");
//                String catalogName = rs.getString("TABLE_CAT");
//                String remarks = rs.getString("REMARKS");
//
//                if (database.isSystemTable(catalogName, schemaName, name) ||
//                        database.isLiquibaseTable(name) ||
//                        database.isSystemView(catalogName, schemaName, name)) {
//                    continue;
//                }
//
//                if ("TABLE".equals(type)) {
//                    Table table = new Table(name);
//                    table.setRemarks(StringUtils.trimToNull(remarks));
//                    table.setDatabase(database);
//                    table.setSchema(schemaName);
//                    snapshot.getTables().add(table);
//                } else if ("VIEW".equals(type)) {
//                    View view = new View(name);
//                    view.setSchema(schemaName);
//                    try {
//                        view.setDefinition(database.
//                                getViewDefinition(schema, name));
//                    } catch (DatabaseException e) {
//                        System.out.println("Error getting view with " + new GetViewDefinitionStatement(schema, name));
//                        throw e;
//                    }
//                    snapshot.getViews().add(view);
//                }
//            }
//        } finally {
//            rs.close();
//        }
//    }
//
//    /**
//     * SQLite specific implementation
//     */
//    @Override
//    protected void readForeignKeys(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        updateListeners("Reading foreign keys for " + snapshot.getDatabase().toString() + " ...");
//        // Foreign keys are not supported in SQLite until now.
//        // ...do nothing here
//    }
//
//    /**
//     * SQLite specific implementation
//     */
//    @Override
//    protected void readPrimaryKeys(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        Database database = snapshot.getDatabase();
//        updateListeners("Reading primary keys for " + database.toString() + " ...");
//
//        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
//        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();
//
//        for (Table table : snapshot.getTables()) {
//            ResultSet rs = databaseMetaData.getPrimaryKeys(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), table.getName());
//
//            try {
//                while (rs.next()) {
//                    String tableName = rs.getString("TABLE_NAME");
//                    String columnName = rs.getString("COLUMN_NAME");
//                    short position = rs.getShort("KEY_SEQ");
//
//                    if (!(database instanceof SQLiteDatabase)) {
//                        position -= 1;
//                    }
//
//                    boolean foundExistingPK = false;
//                    for (PrimaryKey pk : foundPKs) {
//                        if (pk.getTable().getName().equals(tableName)) {
//                            pk.addColumnName(position, columnName);
//
//                            foundExistingPK = true;
//                        }
//                    }
//
//                    if (!foundExistingPK) {
//                        PrimaryKey primaryKey = new PrimaryKey();
//                        primaryKey.setTable(table);
//                        primaryKey.addColumnName(position, columnName);
//                        primaryKey.setName(rs.getString("PK_NAME"));
//
//                        foundPKs.add(primaryKey);
//                    }
//                }
//            } finally {
//                rs.close();
//            }
//
//        }
//
//        snapshot.getPrimaryKeys().addAll(foundPKs);
//    }
//
////    @Override
////    protected void readColumns(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
////        Database database = snapshot.getDatabase();
////        updateListeners("Reading columns for " + database.toString() + " ...");
////
////        if (database instanceof SQLiteDatabase) {
////            // ...work around for SQLite
////            for (Table cur_table : snapshot.getTables()) {
////                Statement selectStatement = null;
////                ResultSet rs = null;
////                try {
////                    selectStatement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
////                    rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), cur_table.getName(), null);
////                    if (rs == null) {
////                        rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), cur_table.getName(), null);
////                    }
////                    while ((rs != null) && rs.next()) {
////                        readColumnInfo(snapshot, schema, rs);
////                    }
////                } finally {
////                    if (rs != null) {
////                        try {
////                            rs.close();
////                        } catch (SQLException ignored) {
////                        }
////                    }
////                    if (selectStatement != null) {
////                        selectStatement.close();
////                    }
////                }
////            }
////        } else {
////            // ...if it is no SQLite database
////            Statement selectStatement = null;
////            ResultSet rs = null;
////            try {
////                selectStatement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
////                rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, null);
////                while (rs.next()) {
////                    readColumnInfo(snapshot, schema, rs);
////                }
////            } finally {
////                if (rs != null) {
////                    try {
////                        rs.close();
////                    } catch (SQLException ignored) {
////                    }
////                }
////                if (selectStatement != null) {
////                    selectStatement.close();
////                }
////            }
////        }
////    }
//
////    private Column readColumnInfo(DatabaseSnapshot snapshot, String schema, ResultSet rs) throws SQLException, DatabaseException {
////        Database database = snapshot.getDatabase();
////        Column columnInfo = new Column();
////
////        String tableName = rs.getString("TABLE_NAME");
////        String columnName = rs.getString("COLUMN_NAME");
////        String schemaName = rs.getString("TABLE_SCHEM");
////        String catalogName = rs.getString("TABLE_CAT");
////
////        String upperCaseTableName = tableName.toUpperCase(Locale.ENGLISH);
////
////        if (database.isSystemTable(catalogName, schemaName, upperCaseTableName) ||
////                database.isLiquibaseTable(upperCaseTableName)) {
////            return null;
////        }
////
////        Table table = snapshot.getTable(tableName);
////        if (table == null) {
////            View view = snapshot.getView(tableName);
////            if (view == null) {
////                LogFactory.getLogger().debug("Could not find table or view " + tableName + " for column " + columnName);
////                return null;
////            } else {
////                columnInfo.setView(view);
////                view.getColumns().add(columnInfo);
////            }
////        } else {
////            columnInfo.setTable(table);
////            table.getColumns().add(columnInfo);
////        }
////
////        columnInfo.setName(columnName);
////        columnInfo.setDataType(rs.getInt("DATA_TYPE"));
////        columnInfo.setColumnSize(rs.getInt("COLUMN_SIZE"));
////        columnInfo.setDecimalDigits(rs.getInt("DECIMAL_POINTS"));
////        Object defaultValue = rs.getObject("COLUMN_DEF");
//////        try {
////            //todo columnInfo.setDefaultValue(TypeConverterFactory.getInstance().findTypeConverter(database).convertDatabaseValueToObject(defaultValue, columnInfo.getDataType(), columnInfo.getColumnSize(), columnInfo.getDecimalDigits(), database));
//////        } catch (ParseException e) {
//////            throw new DatabaseException(e);
//////        }
////
////        int nullable = rs.getInt("NULLABLE");
////        if (nullable == DatabaseMetaData.columnNoNulls) {
////            columnInfo.setNullable(false);
////        } else if (nullable == DatabaseMetaData.columnNullable) {
////            columnInfo.setNullable(true);
////        }
////
////        columnInfo.setPrimaryKey(snapshot.isPrimaryKey(columnInfo));
////        columnInfo.setAutoIncrement(isColumnAutoIncrement(database, schema, tableName, columnName));
////        String typeName = rs.getString("TYPE_NAME");
////        if (columnInfo.isAutoIncrement()) {
////            typeName += "{autoIncrement:true}";
////        }
////        columnInfo.setType(DataTypeFactory.getInstance().parse(typeName));
////
////        return columnInfo;
////    }
//
//    @Override
//    protected void readIndexes(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        Database database = snapshot.getDatabase();
//        updateListeners("Reading indexes for " + database.toString() + " ...");
//
//        for (Table table : snapshot.getTables()) {
//            ResultSet rs = null;
//            Statement statement = null;
//            Map<String, Index> indexMap;
//            try {
//                indexMap = new HashMap<String, Index>();
//
//                // for the odbc driver at http://www.ch-werner.de/sqliteodbc/
//                // databaseMetaData.getIndexInfo is not implemented
//                statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//                String sql = "PRAGMA index_list(" + table.getName() + ");";
//                try {
//                    rs = statement.executeQuery(sql);
//                } catch (SQLException e) {
//                    if (!e.getMessage().equals("query does not return ResultSet")) {
//                        System.err.println(e);
////            			throw e;
//                    }
//                }
//                while ((rs != null) && rs.next()) {
//                    String index_name = rs.getString("name");
//                    boolean index_unique = rs.getBoolean("unique");
//                    sql = "PRAGMA index_info(" + index_name + ");";
//                    Statement statement_2 = null;
//                    ResultSet rs_2 = null;
//                    try {
//                        statement_2 = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//                        rs_2 = statement_2.executeQuery(sql);
//                        while ((rs_2 != null) && rs_2.next()) {
//                            int index_column_seqno = rs_2.getInt("seqno");
////                		int index_column_cid = rs.getInt("cid");
//                            String index_column_name = rs_2.getString("name");
//                            if (index_unique) {
//                                Column column = snapshot.getColumn(table.getName(), index_column_name);
//                                column.setUnique(true);
//                            } else {
//                                Index indexInformation;
//                                if (indexMap.containsKey(index_name)) {
//                                    indexInformation = indexMap.get(index_name);
//                                } else {
//                                    indexInformation = new Index();
//                                    indexInformation.setTable(table);
//                                    indexInformation.setName(index_name);
//                                    indexInformation.setFilterCondition("");
//                                    indexMap.put(index_name, indexInformation);
//                                }
//                                indexInformation.getColumns().add(index_column_seqno, index_column_name);
//                            }
//                        }
//                    } finally {
//                        if (rs_2 != null) {
//                            try {
//                                rs_2.close();
//                            } catch (SQLException ignored) {
//                            }
//                        }
//                        if (statement_2 != null) {
//                            try {
//                                statement_2.close();
//                            } catch (SQLException ignored) {
//                            }
//                        }
//                    }
//
//                }
//            } finally {
//                if (rs != null) {
//                    try {
//                        rs.close();
//                    } catch (SQLException ignored) { }
//                }
//                if (statement != null) {
//                    try {
//                        statement.close();
//                    } catch (SQLException ignored) { }
//                }
//            }
//
//            for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
//                snapshot.getIndexes().add(entry.getValue());
//            }
//        }
//
//        //remove PK indexes
//        Set<Index> indexesToRemove = new HashSet<Index>();
//        for (Index index : snapshot.getIndexes()) {
//            for (PrimaryKey pk : snapshot.getPrimaryKeys()) {
//                if (index.getTable().getName().equalsIgnoreCase(pk.getTable().getName())
//                        && index.getColumnNames().equals(pk.getColumnNames())) {
//                    indexesToRemove.add(index);
//                }
//            }
//        }
//        snapshot.getIndexes().removeAll(indexesToRemove);
//    }
//
//    @Override
//    protected void readSequences(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException {
//        Database database = snapshot.getDatabase();
//        updateListeners("Reading sequences for " + database.toString() + " ...");
//
//        String convertedSchemaName = database.convertRequestedSchemaToSchema(schema);
//
//        if (database.supportsSequences()) {
//            //noinspection unchecked
//            List<String> sequenceNamess = (List<String>) ExecutorService.getInstance().getExecutor(database).queryForList(new SelectSequencesStatement(schema), String.class);
//
//
//            for (String sequenceName : sequenceNamess) {
//                Sequence seq = new Sequence();
//                seq.setName(sequenceName.trim());
//                seq.setName(convertedSchemaName);
//
//                snapshot.getSequences().add(seq);
//            }
//        }
//    }
//
}
