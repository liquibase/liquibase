package liquibase.database;

import liquibase.exception.JDBCException;

import java.sql.Connection;
import java.sql.Types;

public class CacheDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "cache";

    @Override
    protected String getBlobType() {
        return "LONGVARBINARY";
    }

    @Override
    protected String getBooleanType() {
        return "INTEGER";
    }

    @Override
    protected String getClobType() {
        return "LONGVARCHAR";
    }

    @Override
    protected String getCurrencyType() {
        return "MONEY";
    }

    @Override
    protected String getDateTimeType() {
        return "DATETIME";
    }

    @Override
    protected String getUUIDType() {
        return "RAW";
    }

    public String getCurrentDateTimeFunction() {
        return "SYSDATE";
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:Cache")) {
            return "com.intersys.jdbc.CacheDriver";
        }
        return null;
    }

    public String getProductName() {
        return "Cache";
    }

    public String getTypeName() {
        return "cache";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn)
            throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public String getLineComment() {
        return "--";
    }

    public String getFalseBooleanValue() {
        return "0";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public String getSchemaName() throws JDBCException {
        return "";
    }

    public boolean supportsSequences() {
        return false;
    }

    @Override
    protected void dropSequences(DatabaseConnection conn) throws JDBCException {

    }


    public boolean supportsTablespaces() {
        return false;
    }
}
