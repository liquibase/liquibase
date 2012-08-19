package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.core.OracleDatabase;
import liquibase.database.structure.*;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.util.JdbcUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class OracleDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {

//    private List<String> integerList = new ArrayList<String>();
//
    public boolean supports(Database database) {
        return database instanceof OracleDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }
    //
//
//    /**
//     * Oracle specific implementation
//     */
////    @Override
////    protected Object readDefaultValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, DatabaseException {
////        super.readDefaultValue(columnInfo, rs, database);
////
////        // Exclusive setting for oracle INTEGER type
////        // Details:
////        // INTEGER means NUMBER type with 'data_precision IS NULL and scale = 0'
////        if (columnInfo.getDataType() == Types.INTEGER) {
////            columnInfo.setType(DataTypeFactory.getInstance().parse("INTEGER"));
////        }
////
////        String columnTypeName = rs.getString("TYPE_NAME");
////        if ("VARCHAR2".equals(columnTypeName)) {
////            int charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");
////            int columnSize = rs.getInt("COLUMN_SIZE");
////            if (columnSize == charOctetLength) {
////                columnInfo.setLengthSemantics(Column.ColumnSizeUnit.BYTE);
////            } else {
////                columnInfo.setLengthSemantics(Column.ColumnSizeUnit.CHAR);
////            }
////        }
////    }
//
//    @Override
//    protected void readUniqueConstraints(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        Database database = snapshot.getDatabase();
//        updateListeners("Reading unique constraints for " + database.toString() + " ...");
//        List<UniqueConstraint> foundUC = new ArrayList<UniqueConstraint>();
//
//        Connection jdbcConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
//
//        PreparedStatement statement = null;
//        ResultSet rs = null;
//
//        // Setting default schema name. Needed for correct statement generation
//        if (schema == null)
//            schema = database.convertRequestedSchemaToSchema(schema);
//
//        try {
//            String query = "select uc.constraint_name,uc.table_name,uc.status,uc.deferrable,uc.deferred,ui.tablespace_name from all_constraints uc, all_cons_columns ucc, all_indexes ui where uc.constraint_type='U' and uc.index_name = ui.index_name and uc.constraint_name = ucc.constraint_name and uc.owner = '" + schema + "' and ui.table_owner = '" + schema + "' and ucc.owner = '" + schema + "'";
//            statement = jdbcConnection.prepareStatement(query);
//            rs = statement.executeQuery();
//            while (rs.next()) {
//                String constraintName = rs.getString("constraint_name");
//                String tableName = rs.getString("table_name");
//                String status = rs.getString("status");
//                String deferrable = rs.getString("deferrable");
//                String deferred = rs.getString("deferred");
//                String tablespace = rs.getString("tablespace_name");
//                UniqueConstraint constraintInformation = new UniqueConstraint();
//                constraintInformation.setName(constraintName);
//                constraintInformation.setTablespace(tablespace);
//                if (!database.isSystemTable(null, schema, tableName) && !database.isLiquibaseTable(tableName)) {
//                    Table table = snapshot.getTable(tableName);
//                    if (table == null) {
//                        continue; //probably different schema
//                    }
//                    constraintInformation.setTable(table);
//                    constraintInformation.setDisabled("DISABLED".equals(status));
//                    if ("DEFERRABLE".equals(deferrable)) {
//                        constraintInformation.setDeferrable(true);
//                        constraintInformation.setInitiallyDeferred("DEFERRED".equals(deferred));
//                    }
//                    getColumnsForUniqueConstraint(jdbcConnection, constraintInformation, schema);
//                    foundUC.add(constraintInformation);
//                }
//            }
//            snapshot.getUniqueConstraints().addAll(foundUC);
//        } finally {
//            try {
//                rs.close();
//            } catch (SQLException ignored) { }
//            if (statement != null) {
//                statement.close();
//            }
//
//        }
//    }
//
//    protected void getColumnsForUniqueConstraint(Connection jdbcConnection, UniqueConstraint constraint, String schema) throws SQLException {
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        try {
//            stmt = jdbcConnection.prepareStatement("select ucc.column_name from all_cons_columns ucc where ucc.constraint_name=? and ucc.owner=? order by ucc.position");
//            stmt.setString(1, constraint.getName());
//            stmt.setString(2, schema);
//            rs = stmt.executeQuery();
//            while (rs.next()) {
//                String columnName = rs.getString("column_name");
//                constraint.getColumns().add(columnName);
//            }
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ignored) {
//                }
//            }
//            if (stmt != null)
//                stmt.close();
//        }
//    }
//
//    @Override
//    protected void readColumns(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
//        findIntegerColumns(snapshot, schema);
//        super.readColumns(snapshot, schema, databaseMetaData);
//
//        /*
//          * Code Description:
//          * Finding all 'tablespace' attributes of column's PKs
//          * */
//        Database database = snapshot.getDatabase();
//        Statement statement = null;
//        ResultSet rs = null;
//        try {
//            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//
//            // Setting default schema name. Needed for correct statement generation
//            if (schema == null)
//                schema = database.convertRequestedSchemaToSchema(schema);
//
//            String query = "select ui.tablespace_name TABLESPACE, ucc.table_name TABLE_NAME, ucc.column_name COLUMN_NAME FROM all_indexes ui , all_constraints uc , all_cons_columns ucc where uc.constraint_type = 'P' and ucc.constraint_name = uc.constraint_name and uc.index_name = ui.index_name and uc.owner = '" + schema + "' and ui.table_owner = '" + schema + "' and ucc.owner = '" + schema + "'";
//            rs = statement.executeQuery(query);
//
//            while (rs.next()) {
//                Column column = snapshot.getColumn(rs.getString("TABLE_NAME"), rs.getString("COLUMN_NAME"));
//                // setting up tablespace property to column, to configure it's PK-index
//                if (column == null) {
//                    continue; //probably a different schema
//                }
//                column.setTablespace(rs.getString("TABLESPACE"));
//            }
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ignore) {
//                }
//            }
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException ignore) {
//                }
//            }
//        }
//
//    }
//
//    /**
//     * Method finds all INTEGER columns in snapshot's database
//     *
//     * @param snapshot current database snapshot
//     * @return String list with names of all INTEGER columns
//     * @throws java.sql.SQLException execute statement error
//     */
//    private List<String> findIntegerColumns(DatabaseSnapshot snapshot, String schema) throws SQLException, DatabaseException {
//
//        Database database = snapshot.getDatabase();
//        // Setting default schema name. Needed for correct statement generation
//        if (schema == null) {
//            schema = database.convertRequestedSchemaToSchema(schema);
//        }
//        Statement statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//        ResultSet integerListRS = null;
//        // Finding all columns created as 'INTEGER'
//        try {
//            integerListRS = statement.executeQuery("select TABLE_NAME, COLUMN_NAME from all_tab_columns where data_precision is null and data_scale = 0 and data_type = 'NUMBER' and owner = '" + schema + "'");
//            while (integerListRS.next()) {
//                integerList.add(integerListRS.getString("TABLE_NAME") + "." + integerListRS.getString("COLUMN_NAME"));
//            }
//        } finally {
//            if (integerListRS != null) {
//                try {
//                    integerListRS.close();
//                } catch (SQLException ignore) {
//                }
//            }
//
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException ignore) {
//                }
//            }
//        }
//
//
//        return integerList;
//    }
//
////    @Override
////    protected DatabaseDataType readDataType(ResultSet rs, Database database) throws SQLException {
////        if (integerList.contains(column.getTable().getName() + "." + column.getName())) {
////            column.setDataType(Types.INTEGER);
////        } else {
////            column.setDataType(rs.getInt("DATA_TYPE"));
////        }
////        column.setColumnSize(rs.getInt("COLUMN_SIZE"));
////        column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
////
////        // Set true, if precision should be initialize
////        column.setInitPrecision(
////                !((column.getDataType() == Types.DECIMAL ||
////                        column.getDataType() == Types.NUMERIC ||
////                        column.getDataType() == Types.REAL) && rs.getString("DECIMAL_DIGITS") == null)
////        );
////    }
//
//    @Override
//    public List<ForeignKey> getAdditionalForeignKeys(String schemaName, Database database) throws DatabaseException {
//        List<ForeignKey> foreignKeys = super.getAdditionalForeignKeys(schemaName, database);
//
//        // Setting default schema name. Needed for correct statement generation
//        if (schemaName == null) {
//            schemaName = database.convertRequestedSchemaToSchema(schemaName);
//        }
//
//        // Create SQL statement to select all FKs in database which referenced to unique columns
//        String query = "select uc_fk.constraint_name FK_NAME,uc_fk.owner FKTABLE_SCHEM,ucc_fk.table_name FKTABLE_NAME,ucc_fk.column_name FKCOLUMN_NAME,decode(uc_fk.deferrable, 'DEFERRABLE', 5 ,'NOT DEFERRABLE', 7 , 'DEFERRED', 6 ) DEFERRABILITY, decode(uc_fk.delete_rule, 'CASCADE', 0,'NO ACTION', 3) DELETE_RULE,ucc_rf.table_name PKTABLE_NAME,ucc_rf.column_name PKCOLUMN_NAME from all_cons_columns ucc_fk,all_constraints uc_fk,all_cons_columns ucc_rf,all_constraints uc_rf where uc_fk.CONSTRAINT_NAME = ucc_fk.CONSTRAINT_NAME and uc_fk.constraint_type='R' and uc_fk.r_constraint_name=ucc_rf.CONSTRAINT_NAME and uc_rf.constraint_name = ucc_rf.constraint_name and uc_rf.constraint_type = 'U' and uc_fk.owner = '" + schemaName + "' and ucc_fk.owner = '" + schemaName + "' and uc_rf.owner = '" + schemaName + "' and ucc_rf.owner = '" + schemaName + "'";
//        Statement statement = null;
//        ResultSet rs = null;
//        try {
//            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//            rs = statement.executeQuery(query);
//            while (rs.next()) {
//                ForeignKeyInfo fkInfo = new ForeignKeyInfo();
//                fkInfo.setReferencesUniqueColumn(true);
//                fkInfo.setFkName(cleanObjectNameFromDatabase(rs.getString("FK_NAME")));
//                fkInfo.setFkSchema(cleanObjectNameFromDatabase(rs.getString("FKTABLE_SCHEM")));
//                fkInfo.setFkTableName(cleanObjectNameFromDatabase(rs.getString("FKTABLE_NAME")));
//                fkInfo.setFkColumn(cleanObjectNameFromDatabase(rs.getString("FKCOLUMN_NAME")));
//
//                fkInfo.setPkTableName(cleanObjectNameFromDatabase(rs.getString("PKTABLE_NAME")));
//                fkInfo.setPkColumn(cleanObjectNameFromDatabase(rs.getString("PKCOLUMN_NAME")));
//
//                fkInfo.setDeferrablility(rs.getShort("DEFERRABILITY"));
//                ForeignKeyConstraintType deleteRule = convertToForeignKeyConstraintType(rs.getInt("DELETE_RULE"));
//                if (rs.wasNull()) {
//                    deleteRule = null;
//                }
//                fkInfo.setDeleteRule(deleteRule);
//                foreignKeys.add(generateForeignKey(fkInfo, database, foreignKeys));
//            }
//        } catch (SQLException e) {
//            throw new DatabaseException("Can't execute selection query to generate list of foreign keys", e);
//        } finally {
//            JdbcUtils.closeResultSet(rs);
//            JdbcUtils.closeStatement(statement);
//        }
//        return foreignKeys;
//    }
//
    @Override
    protected void readIndexes(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        schema = database.correctSchema(schema);
        updateListeners("Reading indexes for " + database.toString() + " ...");

        String query = "select aic.index_name, 3 AS TYPE, aic.table_name, aic.column_name, aic.column_position AS ORDINAL_POSITION, null AS FILTER_CONDITION, ai.tablespace_name AS TABLESPACE, ai.uniqueness FROM all_ind_columns aic, all_indexes ai WHERE aic.table_owner='" + schema.getName() + "' and ai.table_owner='" + schema.getName() + "' and aic.index_name = ai.index_name ORDER BY INDEX_NAME, ORDINAL_POSITION";
        Statement statement = null;
        ResultSet rs = null;
        Map<String, Index> indexMap = null;
        try {
            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
            rs = statement.executeQuery(query);

            indexMap = new HashMap<String, Index>();
            while (rs.next()) {
                String indexName = cleanObjectNameFromDatabase(rs.getString("INDEX_NAME"));
                String tableName = rs.getString("TABLE_NAME");
                String tableSpace = rs.getString("TABLESPACE");
                String columnName = cleanObjectNameFromDatabase(rs.getString("COLUMN_NAME"));
                if (columnName == null) {
                    //nothing to index, not sure why these come through sometimes
                    continue;
                }
                short type = rs.getShort("TYPE");

                boolean nonUnique;

                String uniqueness = rs.getString("UNIQUENESS");

                if ("UNIQUE".equals(uniqueness)) {
                    nonUnique = false;
                } else {
                    nonUnique = true;
                }

                short position = rs.getShort("ORDINAL_POSITION");
                String filterCondition = rs.getString("FILTER_CONDITION");

                if (type == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }

                Index index;
                if (indexMap.containsKey(indexName)) {
                    index = indexMap.get(indexName);
                } else {
                    index = new Index();
                    Table table = snapshot.getDatabaseObject(schema, tableName, Table.class);
                    if (table == null) {
                        continue; //probably different schema
                    }
                    index.setTable(table);
                    index.setTablespace(tableSpace);
                    index.setName(indexName);
                    index.setUnique(!nonUnique);
                    index.setFilterCondition(filterCondition);
                    indexMap.put(indexName, index);
                }

                for (int i = index.getColumns().size(); i < position; i++) {
                    index.getColumns().add(null);
                }
                index.getColumns().set(position - 1, columnName);
            }
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(statement);
        }

        for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
            snapshot.addDatabaseObjects(entry.getValue());
        }

        /*
          * marks indexes as "associated with" instead of "remove it"
          * Index should have associations with:
          * foreignKey, primaryKey or uniqueConstraint
          * */
        for (Index index : snapshot.getDatabaseObjects(schema, Index.class)) {
            for (PrimaryKey pk : snapshot.getDatabaseObjects(schema, PrimaryKey.class)) {
                if (database.objectNamesEqual(index.getTable().getName(), pk.getTable().getName()) && database.objectNamesEqual(index.getColumnNames(), pk.getColumnNames())) {
                    index.addAssociatedWith(Index.MARK_PRIMARY_KEY);
                }
            }
            for (ForeignKey fk : snapshot.getDatabaseObjects(schema, ForeignKey.class)) {
                if (database.objectNamesEqual(index.getTable().getName(), fk.getForeignKeyTable().getName()) && database.objectNamesEqual(index.getColumnNames(), fk.getForeignKeyColumns())) {
                    index.addAssociatedWith(Index.MARK_FOREIGN_KEY);
                }
            }
            for (UniqueConstraint uc : snapshot.getDatabaseObjects(schema, UniqueConstraint.class)) {
                if (database.objectNamesEqual(index.getTable().getName(), uc.getTable().getName()) && database.objectNamesEqual(index.getColumnNames(), uc.getColumnNames())) {
                    index.addAssociatedWith(Index.MARK_UNIQUE_CONSTRAINT);
                }
            }
        }
    }
//
//    @Override
//    protected void readPrimaryKeys(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        Database database = snapshot.getDatabase();
//        updateListeners("Reading primary keys for " + database.toString() + " ...");
//
//        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
//        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();
//        // Setting default schema name. Needed for correct statement generation
//        if (schema == null)
//            schema = database.convertRequestedSchemaToSchema(schema);
//
//        String query = "select uc.table_name TABLE_NAME,ucc.column_name COLUMN_NAME,ucc.position KEY_SEQ,uc.constraint_name PK_NAME,ui.tablespace_name TABLESPACE from all_constraints uc,all_indexes ui,all_cons_columns ucc where uc.constraint_type = 'P' and uc.index_name = ui.index_name and uc.constraint_name = ucc.constraint_name and uc.owner = '" + schema + "' and ui.table_owner = '" + schema + "' and ucc.owner = '" + schema + "' and uc.table_name = ui.table_name and ui.table_name = ucc.table_name";
//        Statement statement = null;
//        ResultSet rs = null;
//        try {
//            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//            rs = statement.executeQuery(query);
//
//            while (rs.next()) {
//                String tableName = cleanObjectNameFromDatabase(rs.getString("TABLE_NAME"));
//                String tablespace = cleanObjectNameFromDatabase(rs.getString("TABLESPACE"));
//                String columnName = cleanObjectNameFromDatabase(rs.getString("COLUMN_NAME"));
//                short position = rs.getShort("KEY_SEQ");
//
//                boolean foundExistingPK = false;
//                for (PrimaryKey pk : foundPKs) {
//                    if (database.objectNamesEqual(pk.getTable().getName(), tableName)) {
//                        pk.addColumnName(position - 1, columnName);
//
//                        foundExistingPK = true;
//                    }
//                }
//
//                if (!foundExistingPK && !database.isLiquibaseTable(tableName)) {
//                    PrimaryKey primaryKey = new PrimaryKey();
//                    primaryKey.setTablespace(tablespace);
//                    Table table = snapshot.getTable(tableName);
//                    if (table == null) {
//                        continue; //probably a different schema
//                    }
//                    primaryKey.setTable(table);
//                    primaryKey.addColumnName(position - 1, columnName);
//                    primaryKey.setName(database.correctPrimaryKeyName(rs.getString("PK_NAME")));
//
//                    foundPKs.add(primaryKey);
//                }
//            }
//        } finally {
//            JdbcUtils.closeResultSet(rs);
//            JdbcUtils.closeStatement(statement);
//        }
//
//        snapshot.getPrimaryKeys().addAll(foundPKs);
//	}
}