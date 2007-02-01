package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class will handle all the database related tasks for the Oracle database. This class has
 * the methods to generate the statements specific for the oracle database.
 */
public class OracleDatabase extends AbstractDatabase {

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

    protected String getDateType() {
        return "DATE";
    }

    protected String getDateTimeType() {
        return "TIMESTAMP";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws SQLException{
        return "oracle".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName());
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
}
