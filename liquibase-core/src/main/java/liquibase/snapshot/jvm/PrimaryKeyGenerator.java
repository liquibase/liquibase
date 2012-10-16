package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrimaryKeyGenerator extends JdbcSnapshotGenerator {

    public PrimaryKeyGenerator() {
        super(PrimaryKey.class, new Class[]{Table.class});
    }

//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException {
//         return chain.has(example, snapshot);//snapshot( example, database, snapshot) != null;
//    }

    public DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        if (example instanceof Table) {
            return addToTable((Table) chain.snapshot(example, snapshot), snapshot);
        } else if (example instanceof PrimaryKey) {
            return snapshotPrimaryKey((PrimaryKey) example, snapshot);
        } else {
            throw new UnexpectedLiquibaseException("Unexpected example type: " + example.getClass().getName());
        }
    }

    protected PrimaryKey snapshotPrimaryKey(PrimaryKey example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Schema schema = example.getSchema();
        String searchTableName = null;
        if (example.getTable() != null) {
            searchTableName = example.getTable().getName();
        }

        ResultSet rs = null;
        try {
            DatabaseMetaData metaData = getMetaData(database);
            rs = metaData.getPrimaryKeys(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), searchTableName);
            PrimaryKey returnKey = null;
            while (rs.next()) {
                if (example.getName().equals(rs.getString("PK_NAME"))) {
                    continue;
                }
                String columnName = cleanNameFromDatabase(rs.getString("COLUMN_NAME"), database);
                short position = rs.getShort("KEY_SEQ");

                if (returnKey == null) {
                    returnKey = new PrimaryKey();
                    CatalogAndSchema tableSchema = database.getSchemaFromJdbcInfo(rs.getString("TABLE_CAT"), rs.getString("TABLE_SCHEMA"));
                    returnKey.setTable((Table) snapshot.snapshot(new Table().setName(rs.getString("TABLE_NAME")).setSchema(new Schema(tableSchema.getCatalogName(), tableSchema.getSchemaName()))));
                    returnKey.setName(database.correctObjectName(rs.getString("PK_NAME"), PrimaryKey.class));
                }
                returnKey.addColumnName(position - 1, columnName);
            }

            rs.close();
            return returnKey;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {

            }
        }
    }

    protected Table addToTable(Table table, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (table == null) {
            return null;
        }
        Database database = snapshot.getDatabase();
        Schema schema = table.getSchema();

        ResultSet rs = null;
        try {
            DatabaseMetaData metaData = getMetaData(database);
            rs = metaData.getPrimaryKeys(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), table.getName());
            if (rs.next()) {
                table.setPrimaryKey(snapshot.snapshot(new PrimaryKey().setName(rs.getString("PK_NAME")).setTable(table)));
            }

            rs.close();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {

            }
        }

        return table;
    }

    //FROM SQLIteDatabaseSnapshotGenerator
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

//    FROM MySQLDatabaseSnapshotGenerator
//    protected boolean includeInSnapshot(DatabaseObject obj) {
//        if (obj instanceof Index && obj.getName().equals("PRIMARY")) {
//            return false;
//        }
//        return super.includeInSnapshot(obj);
//    }

    //below code was in OracleDatabaseSnapshotGenerator
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
