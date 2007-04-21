package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class MSSQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";
    private Set<String> systemTablesAndViews = new HashSet<String>();

    public MSSQLDatabase() {
        systemTablesAndViews.add("syscolumns");
        systemTablesAndViews.add("syscomments");
        systemTablesAndViews.add("sysdepends");
        systemTablesAndViews.add("sysfilegroups");
        systemTablesAndViews.add("sysfiles");
        systemTablesAndViews.add("sysfiles1");
        systemTablesAndViews.add("sysforeignkeys");
        systemTablesAndViews.add("sysfulltextcatalogs");
        systemTablesAndViews.add("sysfulltextnotify");
        systemTablesAndViews.add("sysindexes");
        systemTablesAndViews.add("sysindexkeys");
        systemTablesAndViews.add("sysmembers");
        systemTablesAndViews.add("sysobjects");
        systemTablesAndViews.add("syspermissions");
        systemTablesAndViews.add("sysproperties");
        systemTablesAndViews.add("sysprotects");
        systemTablesAndViews.add("sysreferences");
        systemTablesAndViews.add("systypes");
        systemTablesAndViews.add("sysusers");

        systemTablesAndViews.add("syssegments");
        systemTablesAndViews.add("sysconstraints");        
    }


    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

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

    public String getCatalogName() throws SQLException {
        return getConnectionUsername();
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

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE "+tableName;
    }
}
