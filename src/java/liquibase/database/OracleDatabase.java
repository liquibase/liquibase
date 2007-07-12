package liquibase.database;

import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;

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

    public boolean supportsSequences() {
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


    protected String getDateType() {
        return "DATE";
    }

    protected String getTimeType() {
        return "DATE";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:oracle")) {
            return "oracle.jdbc.OracleDriver";
        }
        return null;
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


    public String getSchemaName() throws JDBCException {
        return super.getSchemaName().toUpperCase();
    }

    /**
     * Return an Oracle date literal with the same value as a string formatted using ISO 8601.
     *
     * Convert an ISO8601 date string to one of the following results:
     * to_date('1995-05-23', 'YYYY-MM-DD')
     * to_date('1995-05-23 09:23:59', 'YYYY-MM-DD HH24:MI:SS')
     *
     * Implementation restriction:
     * Currently, only the following subsets of ISO8601 are supported:
     * YYYY-MM-DD
     * YYYY-MM-DDThh:mm:ss
     */
    public String getDateLiteral(String isoDate) {
        String normalLiteral = super.getDateLiteral(isoDate);

        if (isDateOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'YYYY-MM-DD')");
            return val.toString();
        } else if (isTimeOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'HH24:MI:SS')");
            return val.toString();
        } else if (isDateTime(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'YYYY-MM-DD HH24:MI:SS')");
            return val.toString();
        } else {
            return "UNSUPPORTED:" + isoDate;
        }
    }

    protected String getSelectChangeLogLockSQL() {
        return (super.getSelectChangeLogLockSQL() + " for update").toUpperCase();
    }

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName + " CASCADE CONSTRAINTS";
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


    public String createFindSequencesSQL() throws JDBCException {
         return "SELECT SEQUENCE_NAME FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = '"+getSchemaName()+"'";
    }
}
