package liquibase.database;

import liquibase.migrator.UnsupportedChangeException;
import liquibase.migrator.change.DropForeignKeyConstraintChange;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class MSSQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";
    private Set<String> systemTablesAndViews = new HashSet<String>();

    public String getProductName() {
        return "Microsoft SQL";
    }

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
        return null;
    }

    public String getCatalogName() throws SQLException {
        return getConnection().getCatalog();
    }

    public String getFalseBooleanValue() {
        return "0";
    }

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName;
    }

    protected void dropForeignKeys(Connection conn) throws SQLException {
        Statement dropStatement = null;
        PreparedStatement fkStatement = null;
        ResultSet rs = null;
        try {
            dropStatement = conn.createStatement();

            fkStatement = conn.prepareStatement("select TABLE_NAME, CONSTRAINT_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where CONSTRAINT_TYPE='FOREIGN KEY' AND TABLE_CATALOG=?");
            fkStatement.setString(1, getCatalogName());
            rs = fkStatement.executeQuery();
            while (rs.next()) {
                DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
                dropFK.setBaseTableName(rs.getString("TABLE_NAME"));
                dropFK.setConstraintName(rs.getString("CONSTRAINT_NAME"));

                try {
                    dropStatement.execute(dropFK.generateStatements(this)[0]);
                } catch (UnsupportedChangeException e) {
                    throw new SQLException(e.getMessage());
                }
            }
        } finally {
            if (dropStatement != null) {
                dropStatement.close();
            }
            if (fkStatement != null) {
                fkStatement.close();
            }
            if (rs != null) {
                rs.close();
            }
        }

    }
}
