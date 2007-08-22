package liquibase.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import liquibase.migrator.change.DropForeignKeyConstraintChange;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;
import liquibase.migrator.exception.UnsupportedChangeException;

/**
 * Encapsulates MS-SQL database support.
 */
public class MSSQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";
    private Set<String> systemTablesAndViews = new HashSet<String>();

    public String getProductName() {
        return "Microsoft SQL";
    }

    public String getTypeName() {
        return "mssql";
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

    public boolean supportsSequences() {
        return false;
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sqlserver")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (url.startsWith("jdbc:jtds:sqlserver")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }
        return null;
    }

    protected String getDateTimeType() {
        return "DATETIME";
    }


    protected String getTimeType() {
        return "DATETIME";
    }


    protected String getDateType() {
        return "SMALLDATETIME";
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

    public String getSchemaName() throws JDBCException {
        return null;
    }

    public String getCatalogName() throws JDBCException {
        try {
            return getConnection().getCatalog();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public String getTrueBooleanValue() {
        return "1";
    }
    
    public String getFalseBooleanValue() {
        return "0";
    }

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName;
    }

    public String getConcatSql(String ... values) {
        StringBuffer returnString = new StringBuffer();
        for (String value : values) {
            returnString.append(value).append(" + ");
        }

        return returnString.toString().replaceFirst(" \\+ $", "");
    }

    @Override
    protected void dropSequences(Connection conn) throws JDBCException, MigrationFailedException {
    }

//    protected void dropForeignKeys(Connection conn) throws JDBCException {
//        Statement dropStatement = null;
//        PreparedStatement fkStatement = null;
//        ResultSet rs = null;
//        try {
//            dropStatement = conn.createStatement();
//
//            fkStatement = conn.prepareStatement("select TABLE_NAME, CONSTRAINT_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where CONSTRAINT_TYPE='FOREIGN KEY' AND TABLE_CATALOG=?");
//            fkStatement.setString(1, getCatalogName());
//            rs = fkStatement.executeQuery();
//            while (rs.next()) {
//                DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
//                dropFK.setBaseTableName(rs.getString("TABLE_NAME"));
//                dropFK.setConstraintName(rs.getString("CONSTRAINT_NAME"));
//
//                try {
//                    dropStatement.execute(dropFK.generateStatements(this)[0]);
//                } catch (UnsupportedChangeException e) {
//                    throw new JDBCException(e.getMessage());
//                }
//            }
//        } catch (SQLException e) {
//            throw new JDBCException(e);
//        } finally {
//            try {
//                if (dropStatement != null) {
//                    dropStatement.close();
//                }
//                if (fkStatement != null) {
//                    fkStatement.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//            } catch (SQLException e) {
//                throw new JDBCException(e);
//            }
//        }
//
//    }
}
