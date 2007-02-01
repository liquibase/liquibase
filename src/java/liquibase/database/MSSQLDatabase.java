package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;

public class MSSQLDatabase extends AbstractDatabase {

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    protected boolean supportsSequences() {
        return false;
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws SQLException {
        return conn.getMetaData().getDatabaseProductName().equalsIgnoreCase("Microsoft SQL Server");
    }

    protected String getDateType() {
        return "DATE";
    }

    protected String getDateTimeType() {
        return "DATETIME";
    }

    protected String getBooleanType() {
        return "BIT";
    }

    protected String getCurrencyType() {
        return "MONEY";
    }

    protected String getUUIDType() {
        return "UNIQUEIDENTIFIER";
    }

    protected String getClobType() {
        return "TEXT";
    }

    protected String getBlobType() {
        return "IMAGE";
    }

    public String getCurrentDateTimeFunction() {
        return "GETDATE()";
    }

}
