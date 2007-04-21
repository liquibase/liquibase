package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase extends AbstractDatabase {
    public boolean isCorrectDatabaseImplementation(Connection conn) throws SQLException {
        return "MySQL".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName());
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

    public String getRenameColumnSQL(String tableName, String oldColumnName, String newColumnName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table ").append(tableName);
        buffer.append(" change ");
        buffer.append(oldColumnName).append(" ");
        buffer.append(newColumnName);
        buffer.append(" ");
        buffer.append(getColumnDataType(tableName, oldColumnName));
        return buffer.toString();
    }

      public String getRenameTableSQL(String oldTableName, String newTableName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table ").append(oldTableName).append(" rename ").append(newTableName);
        return buffer.toString();
    }
}
