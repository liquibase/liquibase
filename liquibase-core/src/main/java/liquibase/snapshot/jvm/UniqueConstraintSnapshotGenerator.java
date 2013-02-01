package liquibase.snapshot.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UniqueConstraintSnapshotGenerator extends JdbcSnapshotGenerator {

    public UniqueConstraintSnapshotGenerator() {
        super(UniqueConstraint.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        UniqueConstraint exampleConstraint = (UniqueConstraint) example;
        Table table = exampleConstraint.getTable();

        List<Map> metadata = listColumns(exampleConstraint, database);

        if (metadata.size() == 0) {
            return null;
        }
        UniqueConstraint constraint = new UniqueConstraint();
        constraint.setTable(table);
        constraint.setName(example.getName());
        for (Map<String, Object> col : metadata) {
            constraint.getColumns().add((String) col.get("COLUMN_NAME"));
        }

        Index exampleIndex = new Index().setTable(constraint.getTable());
        exampleIndex.getColumns().addAll(constraint.getColumns());
        constraint.setBackingIndex(exampleIndex);


        return constraint;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {

        if (!snapshot.getSnapshotControl().shouldInclude(UniqueConstraint.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema;
            schema = table.getSchema();

            List<Map> metadata = listConstraints(table, database, schema);


            Set<String> seenConstraints = new HashSet<String>();

            for (Map<String, Object> constraint : metadata) {
                UniqueConstraint uq = new UniqueConstraint().setName((String) constraint.get("CONSTRAINT_NAME")).setTable(table);
                if (seenConstraints.add(uq.getName())) {
                    table.getUniqueConstraints().add(uq);
                }
            }
        }
    }

    protected List<Map> listConstraints(Table table, Database database, Schema schema) throws DatabaseException {
        String sql = null;
        if (database instanceof MySQLDatabase || database instanceof PostgresDatabase) {
            sql = "select CONSTRAINT_NAME " +
                    "from information_schema.table_constraints " +
                    "where constraint_schema='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and constraint_type='UNIQUE' " +
                    "and table_name='" + database.correctObjectName(table.getName(), Table.class) + "'";
        } else if (database instanceof MSSQLDatabase) {
            sql = "select Constraint_Name from information_schema.table_constraints " +
                    "where constraint_type = 'Unique' " +
                    "and constraint_schema='"+database.correctObjectName(schema.getName(), Schema.class)+"' "+
                    "and table_name='"+database.correctObjectName(table.getName(), Table.class)+"'";
        } else if (database instanceof OracleDatabase) {
            sql = "select uc.constraint_name, uc.table_name,uc.status,uc.deferrable,uc.deferred,ui.tablespace_name from all_constraints uc, all_cons_columns ucc, all_indexes ui " +
                    "where uc.constraint_type='U' and uc.index_name = ui.index_name and uc.constraint_name = ucc.constraint_name " +
                    "and uc.table_name = '" + database.correctObjectName(table.getName(), Table.class) + "' " +
                    "and uc.owner = '" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and ui.table_owner = '" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and ucc.owner = '" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "'";
        } else if (database instanceof DB2Database) {
            sql = "select distinct k.constname as constraint_name from syscat.keycoluse k, syscat.tabconst t " +
                    "where k.constname = t.constname " +
                    "and t.tabname = '" + database.correctObjectName(table.getName(), Table.class) + "' " +
                    "and t.tabschema = '" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and t.type='U'";
        } else {
            sql = "select CONSTRAINT_NAME, CONSTRAINT_TYPE " +
                    "from information_schema.constraints " +
                    "where constraint_schema='" + database.correctObjectName(schema.getName(), Schema.class) + "' " +
                    "and constraint_catalog='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and constraint_type='UNIQUE' " +
                    "and table_name='" + database.correctObjectName(table.getName(), Table.class) + "'";

        }

        return ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql));
    }

    protected List<Map> listColumns(UniqueConstraint example, Database database) throws DatabaseException {
        Table table = example.getTable();
        Schema schema = table.getSchema();
        String name = example.getName();

        String sql = null;
        if (database instanceof MySQLDatabase) {
            sql = "select const.CONSTRAINT_NAME, COLUMN_NAME " +
                    "from information_schema.table_constraints const " +
                    "join information_schema.key_column_usage col " +
                    "on const.constraint_schema=col.constraint_schema " +
                    "and const.table_name=col.table_name " +
                    "and const.constraint_name=col.constraint_name " +
                    "where const.constraint_schema='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and const.table_name='" + database.correctObjectName(example.getTable().getName(), Table.class) + "' " +
                    "and const.constraint_name='" + database.correctObjectName(name, UniqueConstraint.class) + "'" +
                    "order by ordinal_position";
        } else if (database instanceof MSSQLDatabase) {
            sql = "select TC.Constraint_Name as CONSTRAINT_NAME, CC.Column_Name as COLUMN_NAME from information_schema.table_constraints TC " +
                    "inner join information_schema.constraint_column_usage CC on TC.Constraint_Name = CC.Constraint_Name " +
                    "where TC.constraint_schema='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and TC.table_name='" + database.correctObjectName(example.getTable().getName(), Table.class) + "' " +
                    "and TC.Constraint_Name='" + database.correctObjectName(name, UniqueConstraint.class) + "'" +
                    "order by TC.Constraint_Name";
        } else if (database instanceof OracleDatabase) {
            sql = "select ucc.column_name from all_cons_columns ucc where ucc.constraint_name='"+database.correctObjectName(name, UniqueConstraint.class)+"' and ucc.owner='"+database.correctObjectName(schema.getCatalogName(), Catalog.class)+"' order by ucc.position";
        } else if (database instanceof DB2Database) {
            sql = "select k.colname as column_name from syscat.keycoluse k, syscat.tabconst t " +
                    "where k.constname = t.constname " +
                    "and t.type='U' " +
                    "and k.constname='"+database.correctObjectName(name, UniqueConstraint.class)+"' "+
                    "order by colseq";
        } else {
            sql = "select CONSTRAINT_NAME, COLUMN_LIST as COLUMN_NAME " +
                    "from information_schema.constraints " +
                    "where constraint_schema='" + database.correctObjectName(schema.getName(), Schema.class) + "' " +
                    "and constraint_catalog='" + database.correctObjectName(schema.getCatalogName(), Catalog.class) + "' " +
                    "and constraint_type='UNIQUE' " +
                    "and table_name='" + database.correctObjectName(table.getName(), Table.class) + "' "+
                       "and constraint_name='" + database.correctObjectName(name, UniqueConstraint.class) + "'";
        }
        return ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql));
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
