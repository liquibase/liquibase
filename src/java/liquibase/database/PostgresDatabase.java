package liquibase.database;

import liquibase.migrator.MigrationFailedException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.HashSet;

public class PostgresDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "PostgreSQL";

    private Set<String> systemTablesAndViews = new HashSet<String>();

    public PostgresDatabase() {
        systemTablesAndViews.add("pg_logdir_ls");
    }

    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws SQLException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getMetaData().getDatabaseProductName());
    }

    protected String getBooleanType() {
        return "BOOLEAN";
    }

    protected String getCurrencyType() {
        return "DECIMAL";
    }

    protected String getUUIDType() {
        return null;
    }

    protected String getClobType() {
        return "TEXT";
    }

    protected String getBlobType() {
        return "BYTEA";
    }

    protected String getDateType() {
        return "DATE";
    }

    protected String getDateTimeType() {
        return "TIMESTAMP";
    }

    protected boolean supportsSequences() {
        return true;
    }

    public String getCurrentDateTimeFunction() {
        return "CURRENT_DATE";
    }

    public String getSchemaName() throws SQLException {
        return null;
    }

    public String getCatalogName() throws SQLException {
        return "PUBLIC";
    }

    public String getDatabaseChangeLogTableName() {
        return super.getDatabaseChangeLogTableName().toLowerCase();
    }

    public String getDatabaseChangeLogLockTableName() {
        return super.getDatabaseChangeLogLockTableName().toLowerCase();
    }

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName;
    }

    public void dropDatabaseObjects() throws SQLException, MigrationFailedException {
        Connection conn = getConnection();
        Statement dropStatement = conn.createStatement();
        try {
            dropStatement.executeUpdate("DROP OWNED BY " + getConnectionUsername());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dropStatement != null) {
                dropStatement.close();
            }
            conn.commit();
        }


    }

    public String getDropNullConstraintSQL(String tableName, String columnName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table ");
        buffer.append(tableName);
        buffer.append(" alter column  ");
        buffer.append(columnName);
        buffer.append(" drop not null");
        return buffer.toString();
    }

    public String getAddNullConstraintSQL(String tableName, String columnName, String defaultNullValue) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table ");
        buffer.append(tableName);
        buffer.append(" alter column  ");
        buffer.append(columnName);
        buffer.append(" set not null");
//        if (defaultNullValue != null) {
//            buffer.append(" SET DEFAULT '").append(defaultNullValue).append("'");
//        }
        return buffer.toString();
    }

    public String getRenameTableSQL(String oldTableName, String newTableName) {
        return "ALTER TABLE "+oldTableName+" RENAME TO "+newTableName;
    }

    public String getDropIndexSQL(String tableName, String indexName) {
        return "DROP INDEX "+indexName;
    }
}
