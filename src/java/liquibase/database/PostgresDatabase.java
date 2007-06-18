package liquibase.database;

import liquibase.migrator.exception.MigrationFailedException;
import liquibase.migrator.exception.JDBCException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates PostgreSQL database support.
 */
public class PostgresDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "PostgreSQL";

    private Set<String> systemTablesAndViews = new HashSet<String>();

    public PostgresDatabase() {
        systemTablesAndViews.add("pg_logdir_ls");
    }

    public String getProductName() {
        return "PostgreSQL";
    }

    public String getTypeName() {
        return "postgresql";
    }

    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
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

    protected String getDateTimeType() {
        return "TIMESTAMP WITH TIME ZONE";
    }

    protected boolean supportsSequences() {
        return true;
    }

    public String getCurrentDateTimeFunction() {
        return "NOW()";
    }

    public String getSchemaName() throws JDBCException {
        return null;
    }

    public String getCatalogName() throws JDBCException {
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

    public void dropDatabaseObjects() throws JDBCException, MigrationFailedException {
        Connection conn = getConnection();
        Statement dropStatement = null;
        try {
            dropStatement = conn.createStatement();
            dropStatement.executeUpdate("DROP OWNED BY " + getConnectionUsername());
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            try {
                if (dropStatement != null) {
                    dropStatement.close();
                }
                conn.commit();
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        }


    }
}
