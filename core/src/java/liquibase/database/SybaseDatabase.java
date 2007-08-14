package liquibase.database;

import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;

import java.sql.Connection;
import java.sql.SQLException;

public class SybaseDatabase extends MSSQLDatabase {
    public String getProductName() {
        return "Sybase SQL Server";
    }

    public String getTypeName() {
        return "sybase";
    }
    
    public void setConnection(Connection connection) {
        super.setConnection(new SybaseConnection(connection));
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
    
    protected String getCreateChangeLogSQL() {
        return ("CREATE TABLE DATABASECHANGELOG (id varchar(150) not null, " +
                "author varchar(150) not null, " +
                "filename varchar(255) not null, " +
                "dateExecuted " + getDateTimeType() + " not null, " +
                "md5sum varchar(32) null, " +
                "description varchar(255) null, " +
                "comments varchar(255) null, " +
                "tag varchar(255) null, " +
                "liquibase varchar(10) null, " +
                "primary key(id, author, filename))").toUpperCase();
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
    public void dropDatabaseObjects() throws JDBCException, MigrationFailedException {
        Connection conn = getConnection();
        try {
            //dropForeignKeys(conn);
            dropViews(conn);
            dropTables(conn);

            if (this.supportsSequences()) {
                dropSequences(conn);
            }

            changeLogTableExists = false;
        } finally {
            try {
                conn.commit();
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        }
    }


    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        String dbProductName = getDatabaseProductName(conn);
        return "Adaptive Server Enterprise".equals(dbProductName) ||
            "sql server".equals(dbProductName);
    }
}
