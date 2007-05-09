package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "MySQL";

    public String getProductName() {
        return "MySQL";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws SQLException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getMetaData().getDatabaseProductName());
    }

    protected String getBooleanType() {
        return "TINYINT(1)";
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
        return "BLOB";
    }

    protected String getDateType() {
        return "DATE";
    }

    protected String getDateTimeType() {
        return "DATETIME";
    }

    protected boolean supportsSequences() {
        return false;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        return "NOW()";
    }

    public String getLineComment() {
        return "==";
    }

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName;
    }

}
