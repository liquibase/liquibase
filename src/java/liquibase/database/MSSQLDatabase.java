package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;

public class MSSQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    protected boolean supportsSequences() {
        return false;
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws SQLException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getMetaData().getDatabaseProductName());
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

    public String getAutoIncrementClause() {
        return "IDENTITY";
    }

    public String getSchemaName() throws SQLException {
        return "dbo";
    }

    public String getFalseBooleanValue() {
        return "0";
    }

    public String getRenameTableSQL(String oldTableName, String newTableName) {
        return "sp_rename '"+oldTableName+"', "+newTableName;
    }

    public String getRenameColumnSQL(String tableName, String oldColumnName, String newColumnName) {
        return "sp_rename '"+tableName+"."+oldColumnName+"', "+newColumnName;
    }

    public String getDropIndexSQL(String tableName, String indexName) {
        return "DROP INDEX "+tableName+"."+indexName;
    }


}
