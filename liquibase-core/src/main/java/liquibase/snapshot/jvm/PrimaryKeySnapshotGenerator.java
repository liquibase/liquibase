package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class PrimaryKeySnapshotGenerator extends JdbcSnapshotGenerator {

    public PrimaryKeySnapshotGenerator() {
        super(PrimaryKey.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Schema schema = example.getSchema();
        String searchTableName = null;
        if (((PrimaryKey) example).getTable() != null) {
            searchTableName = ((PrimaryKey) example).getTable().getName();
            searchTableName = database.correctObjectName(searchTableName, Table.class);
        }

        List<CachedRow> rs = null;
        try {
            JdbcDatabaseSnapshot.CachingDatabaseMetaData metaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();
            rs = metaData.getPrimaryKeys(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), searchTableName);
            PrimaryKey returnKey = null;
            for (CachedRow row : rs) {
                if (example.getName() != null && !example.getName().equalsIgnoreCase(row.getString("PK_NAME"))) {
                    continue;
                }
                String columnName = cleanNameFromDatabase(row.getString("COLUMN_NAME"), database);
                short position = row.getShort("KEY_SEQ");

                if (returnKey == null) {
                    returnKey = new PrimaryKey();
                    CatalogAndSchema tableSchema = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"));
                    returnKey.setTable((Table) new Table().setName(row.getString("TABLE_NAME")).setSchema(new Schema(tableSchema.getCatalogName(), tableSchema.getSchemaName())));
                    returnKey.setName(row.getString("PK_NAME"));
                }

                if (database instanceof SQLiteDatabase) { //SQLite is zero based position?
                    position = (short) (position + 1);
                }

                returnKey.addColumn(position - 1, new Column(columnName).setRelation(((PrimaryKey) example).getTable()));
            }

            if (returnKey != null) {
                Index exampleIndex = new Index().setTable(returnKey.getTable());
                exampleIndex.setColumns(returnKey.getColumns());
                if (database instanceof MSSQLDatabase) { //index name matches PK name for better accuracy
                    exampleIndex.setName(returnKey.getName());
                }
                returnKey.setBackingIndex(exampleIndex);
            }

            return returnKey;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(PrimaryKey.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema = table.getSchema();

            List<CachedRow> rs = null;
            try {
                JdbcDatabaseSnapshot.CachingDatabaseMetaData metaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();
                rs = metaData.getPrimaryKeys(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), table.getName());
                if (rs.size() > 0) {
                    table.setPrimaryKey(new PrimaryKey().setName(rs.get(0).getString("PK_NAME")).setTable(table));
                }
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }

        }
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
