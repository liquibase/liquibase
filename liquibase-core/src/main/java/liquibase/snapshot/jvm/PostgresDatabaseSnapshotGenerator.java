package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.structure.Table;
import liquibase.database.structure.UniqueConstraint;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class PostgresDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {

    public boolean supports(Database database) {
        return database instanceof PostgresDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    protected String convertTableNameToDatabaseTableName(String tableName) {
        return tableName;
    }

    @Override
    protected String convertColumnNameToDatabaseTableName(String columnName) {
        return columnName;
    }

    @Override
    protected String convertPrimaryKeyName(String pkName) throws SQLException {
        return pkName;
    }

    @Override
    protected String convertFromDatabaseName(String objectName) {
        if (objectName == null) {
            return null;
        }
        return objectName.replaceAll("\"", "");
    }

    /**
     *
     */
    @Override
    protected void readUniqueConstraints(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
        Database database = snapshot.getDatabase();
        updateListeners("Reading unique constraints for " + database.toString() + " ...");
        List<UniqueConstraint> foundUC = new ArrayList<UniqueConstraint>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().prepareStatement("select pgc.conname, pgc.conrelid, pgc.conkey, pgcl.relname from pg_constraint pgc inner join pg_class pgcl on pgcl.oid = pgc.conrelid and pgcl.relkind ='r' where contype = 'u'");
            rs = statement.executeQuery();
            while (rs.next()) {
                String constraintName = rs.getString("conname");
                long conrelid = rs.getLong("conrelid");
                Array keys = rs.getArray("conkey");
                String tableName = rs.getString("relname");
                UniqueConstraint constraintInformation = new UniqueConstraint();
                constraintInformation.setName(constraintName);
                if(!database.isSystemTable(null, schema, tableName)&&!database.isLiquibaseTable(tableName)) {
                    Table table = snapshot.getTable(tableName);
                    if (table == null) {
                        // SKip it  --  the query  above pulls  back more  then the  query for tables &  views in  the super  class
 	 	                continue;
                    }
	                constraintInformation.setTable(table);
	                getColumnsForUniqueConstraint(database, conrelid, keys, constraintInformation);
	                foundUC.add(constraintInformation);
                }
            }
            snapshot.getUniqueConstraints().addAll(foundUC);
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) { }
            if (statement != null) {
                statement.close();
            }

        }
    }

    protected void getColumnsForUniqueConstraint(Database database, long conrelid, Array keys, UniqueConstraint constraint) throws SQLException {
        HashMap<Integer, String> columns_map = new HashMap<Integer, String>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String str = null;
            Object arrays = keys.getArray();
            if (arrays instanceof Integer[]) {
                str = StringUtils.join((Integer[])arrays, ",");
            } else if (arrays instanceof int[]) {
                str = StringUtils.join((int[])arrays, ",");
            } else {
                throw new SQLException("Can't detect type of array " + arrays);
            }
            stmt = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().prepareStatement("select attname,attnum from pg_attribute where attrelid = ? and attnum in (" + str + ")");
            stmt.setLong(1, conrelid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                columns_map.put(rs.getInt("attnum"), rs.getString("attname"));
            }
            StringTokenizer str_token = new StringTokenizer(keys.toString().replace("{", "").replace("}", ""), ",");
            while (str_token.hasMoreTokens()) {
                Integer column_id = new Integer(str_token.nextToken());
                constraint.getColumns().add(columns_map.get(column_id));
            }
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) { }
            }
            if (stmt != null)
                stmt.close();
        }
    }
}
