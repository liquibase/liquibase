package liquibase.database;

import liquibase.migrator.exception.MigrationFailedException;
import liquibase.migrator.exception.JDBCException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Encapsulates Oracle database support.
 */
public class OracleDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "oracle";

    public String getProductName() {
        return "Oracle";
    }

    public String getTypeName() {
        return "oracle";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    protected boolean supportsSequences() {
        return true;
    }

    protected String getBooleanType() {
        return "NUMBER(1)";
    }

    protected String getCurrencyType() {
        return "NUMBER(15, 2)";
    }

    protected String getUUIDType() {
        return "RAW(16)";
    }

    protected String getClobType() {
        return "CLOB";
    }

    protected String getBlobType() {
        return "BLOB";
    }

    protected String getDateTimeType() {
        return "TIMESTAMP";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public String getCurrentDateTimeFunction() {
        return "SYSDATE";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    public String getFalseBooleanValue() {
        return "0";
    }

    protected String getSelectChangeLogLockSQL() {
        return (super.getSelectChangeLogLockSQL() + " for update").toUpperCase();
    }

    protected void dropSequences(Connection conn) throws JDBCException, MigrationFailedException {
        ResultSet rs = null;
        Statement selectStatement = null;
        Statement dropStatement = null;
        try {
            selectStatement = conn.createStatement();
            dropStatement = conn.createStatement();
            rs = selectStatement.executeQuery("SELECT SEQUENCE_NAME FROM USER_SEQUENCES");
            while (rs.next()) {
                String sequenceName = rs.getString("SEQUENCE_NAME");
                log.finest("Dropping sequence " + sequenceName);
                String sql = "DROP SEQUENCE " + sequenceName;
                try {
                    dropStatement.executeUpdate(sql);
                } catch (SQLException e) {
                    throw new MigrationFailedException("Error dropping sequence '" + sequenceName + "': " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            try {
                if (selectStatement != null) {
                    selectStatement.close();
                }
                if (dropStatement != null) {
                    dropStatement.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        }
    }
}
