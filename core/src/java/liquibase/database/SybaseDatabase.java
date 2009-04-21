package liquibase.database;

import liquibase.database.statement.RawSqlStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.exception.JDBCException;

import java.sql.Connection;

public class SybaseDatabase extends MSSQLDatabase {


    public SybaseDatabase() {
        systemTablesAndViews.add("sysquerymetrics");        
    }

    public String getProductName() {
        return "Sybase SQL Server";
    }

    public String getTypeName() {
        return "sybase";
    }
    
    public void setConnection(Connection connection) {
        super.setConnection(new SybaseConnectionDelegate(connection));
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sybase")) {
            return "com.sybase.jdbc3.jdbc.SybDriver";
        } else if (url.startsWith("jdbc:jtds:sybase")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }
        return null;
    }
    
    /**
     * Sybase does not support DDL and meta data in transactions properly,
     * as such we turn off the commit and turn on auto commit.
     */
    public boolean supportsDDLInTransaction() {
    	return false;
    }
    
    protected SqlStatement getCreateChangeLogSQL() {
        return new RawSqlStatement(("CREATE TABLE "+escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName())+" (ID VARCHAR(150) NOT NULL, " +
                "AUTHOR VARCHAR(150) NOT NULL, " +
                "FILENAME VARCHAR(255) NOT NULL, " +
                "DATEEXECUTED " + getDateTimeType() + " NOT NULL, " +
                "MD5SUM VARCHAR(32) NULL, " +
                "DESCRIPTION VARCHAR(255) NULL, " +
                "COMMENTS VARCHAR(255) NULL, " +
                "TAG VARCHAR(255) NULL, " +
                "LIQUIBASE VARCHAR(10) NULL, " +
                "PRIMARY KEY(ID, AUTHOR, FILENAME))"));
    }

    protected SqlStatement getCreateChangeLogLockSQL() {
        return new RawSqlStatement(("CREATE TABLE "+escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName())+" (ID INT NOT NULL PRIMARY KEY, LOCKED " + getBooleanType() + " NOT NULL, LOCKGRANTED " + getDateTimeType() + " NULL, LOCKEDBY VARCHAR(255) NULL)"));
    }
    
    /**
     * Drops all objects owned by the connected user.
     * 
     * The Sybase functionality overrides this and does not remove the Foreign Keys.
     * 
     * Unfortunately it appears to be a problem with the Drivers, see the JTDS driver page.
     * http://sourceforge.net/tracker/index.php?func=detail&aid=1471425&group_id=33291&atid=407762
     *
     */
//    public void dropDatabaseObjects(String schema) throws JDBCException {
//        DatabaseConnection conn = getConnection();
//        try {
//            //dropForeignKeys(conn);
//            dropViews(schema, conn);
//            dropTables(schema, conn);
//
//            if (this.supportsSequences()) {
//                dropSequences(schema, conn);
//            }
//
//            changeLogTableExists = false;
//        } finally {
//            try {
//                conn.commit();
//            } catch (SQLException e) {
//                ;
//            }
//        }
//    }


    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        String dbProductName = getDatabaseProductName(conn);
        return 
        	"Sybase SQL Server".equals(dbProductName) ||
            "sql server".equals(dbProductName);
    }

    public boolean supportsTablespaces() {
        return true;
    }

}
