package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.UniqueConstraint;

public class UniqueConstraintSnapshotGenerator extends JdbcDatabaseObjectSnapshotGenerator<UniqueConstraint> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean has(DatabaseObject container, UniqueConstraint example, Database database) throws DatabaseException {
        return get(container,  example, database) != null;
    }

    public UniqueConstraint[] get(DatabaseObject container, Database database) {
        return new UniqueConstraint[0];  //TODO
    }

    public UniqueConstraint get(DatabaseObject container, UniqueConstraint example, Database database) throws DatabaseException {
        return null;  //TODO
    }

    //START CODE FROM PostgresDatabseSnapshotGenerator
//    protected void readUniqueConstraints(DatabaseSnapshot snapshot, Schema schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
//        Database database = snapshot.getDatabase();
//        updateListeners("Reading unique constraints for " + database.toString() + " ...");
//        List<UniqueConstraint> foundUC = new ArrayList<UniqueConstraint>();
//        PreparedStatement statement = null;
//        ResultSet rs = null;
//        try {
//            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().prepareStatement("select pgc.conname, pgc.conrelid, pgc.conkey, pgcl.relname from pg_constraint pgc inner join pg_class pgcl on pgcl.oid = pgc.conrelid and pgcl.relkind ='r' where contype = 'u'");
//            rs = statement.executeQuery();
//            while (rs.next()) {
//                String constraintName = rs.getString("conname");
//                long conrelid = rs.getLong("conrelid");
//                Array keys = rs.getArray("conkey");
//                String tableName = rs.getString("relname");
//                UniqueConstraint constraintInformation = new UniqueConstraint();
//                constraintInformation.setName(constraintName);
//                if(!database.isSystemTable(schema, tableName)&&!database.isLiquibaseTable(schema, tableName)) {
//                    Table table = snapshot.getDatabaseObject(schema, tableName, Table.class);
//                    if (table == null) {
//                        // SKip it  --  the query  above pulls  back more  then the  query for tables &  views in  the super  class
//                        continue;
//                    }
//                    constraintInformation.setTable(table);
//                    getColumnsForUniqueConstraint(database, conrelid, keys, constraintInformation);
//                    foundUC.add(constraintInformation);
//                }
//            }
//            snapshot.addDatabaseObjects(foundUC.toArray(new UniqueConstraint[foundUC.size()]));
//        }
//        finally {
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//            } catch (SQLException ignored) { }
//            if (statement != null) {
//                statement.close();
//            }
//
//        }
//    }
//
//    protected void getColumnsForUniqueConstraint(Database database, long conrelid, Array keys, UniqueConstraint constraint) throws SQLException {
//        HashMap<Integer, String> columns_map = new HashMap<Integer, String>();
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        try {
//            String str = null;
//            Object arrays = keys.getArray();
//            if (arrays instanceof Integer[]) {
//                str = StringUtils.join((Integer[])arrays, ",");
//            } else if (arrays instanceof int[]) {
//                str = StringUtils.join((int[])arrays, ",");
//            } else {
//                throw new SQLException("Can't detect type of array " + arrays);
//            }
//            stmt = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().prepareStatement("select attname,attnum from pg_attribute where attrelid = ? and attnum in (" + str + ")");
//            stmt.setLong(1, conrelid);
//            rs = stmt.executeQuery();
//            while (rs.next()) {
//                columns_map.put(rs.getInt("attnum"), rs.getString("attname"));
//            }
//            StringTokenizer str_token = new StringTokenizer(keys.toString().replace("{", "").replace("}", ""), ",");
//            while (str_token.hasMoreTokens()) {
//                Integer column_id = new Integer(str_token.nextToken());
//                constraint.getColumns().add(columns_map.get(column_id));
//            }
//        }
//        finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ignored) { }
//            }
//            if (stmt != null)
//                stmt.close();
//        }
//    }
    //END CODE FROM PostgrestDatabaseSnapshotGenerator


    //code from OracleDatabaseSnapshotGenerator
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
}
