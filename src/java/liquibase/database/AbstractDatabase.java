package liquibase.database;

import liquibase.migrator.DatabaseChangeLogLock;
import liquibase.migrator.MigrationFailedException;
import liquibase.migrator.Migrator;
import liquibase.migrator.change.ColumnConfig;
import liquibase.StreamUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This is an abstract class used to abstract the methods supported by all the
 * databases. This class is extended by all the supporting databases and the methods
 * are overriden.
 */
public abstract class AbstractDatabase {

    private Connection connection;
    protected Logger log;
    private boolean changeLogTableExists;
    private boolean changeLogLockTableExists;

    private static boolean outputtedLockWarning = false;


    protected AbstractDatabase() {
        log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);
    }

    public abstract boolean isCorrectDatabaseImplementation(Connection conn) throws SQLException;

    public String getColumnType(ColumnConfig column) {
        if ("boolean".equalsIgnoreCase(column.getType())) {
            return getBooleanType();
        } else if ("currency".equalsIgnoreCase(column.getType())) {
            return getCurrencyType();
        } else if ("UUID".equalsIgnoreCase(column.getType())) {
            return getUUIDType();
        } else if ("BLOB".equalsIgnoreCase(column.getType())) {
            return getBlobType();
        } else if ("CLOB".equalsIgnoreCase(column.getType())) {
            return getClobType();
        } else {
            return column.getType();
        }
    }

    protected abstract String getBooleanType();

    protected abstract String getCurrencyType();

    protected abstract String getUUIDType();

    protected abstract String getClobType();

    protected abstract String getBlobType();

    protected abstract String getDateType();

    protected abstract String getDateTimeType();

    protected abstract boolean supportsSequences();

    protected String getDatabaseName() {
        try {
            return connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot get database name");
        }
    }

    public String getDriverName() throws SQLException {
        return connection.getMetaData().getDriverName();
    }

    public String getConnectionURL() throws SQLException {
        return connection.getMetaData().getURL();
    }

    public String getConnectionUsername() throws SQLException {
        return connection.getMetaData().getUserName();

    }

    public String getSchemaName() throws SQLException {
        return getConnectionUsername();
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection conn) {
        this.connection = conn;
    }

    /**
     * This method will check the database ChangeLog table used to keep track of
     * the changes in the file. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     *
     * @param migrator
     * @throws java.sql.SQLException
     */
    public void checkDatabaseChangeLogTable(Migrator migrator) throws SQLException, IOException {
        Statement statement = null;
        Connection connection = getConnection();
        ResultSet checkTableRS = null;
        ResultSet checkColumnsRS = null;
        changeLogTableExists = true;
        List<String> statementsToExecute = new ArrayList<String>();
        boolean wroteToOutput = false;

        try {
            checkTableRS = connection.getMetaData().getTables(getCatalogName(), getSchemaName(), getDatabaseChangeLogTableName(), new String[]{"TABLE"});
            if (checkTableRS.next()) {
                checkColumnsRS = connection.getMetaData().getColumns(getCatalogName(), getSchemaName(), getDatabaseChangeLogTableName(), null);
                boolean hasDescription = false;
                boolean hasComments = false;
                boolean hasTag = false;
                boolean hasLiquibase = false;
                while (checkColumnsRS.next()) {
                    String columnName = checkColumnsRS.getString("COLUMN_NAME");
                    if ("DESCRIPTION".equalsIgnoreCase(columnName)) {
                        hasDescription = true;
                    } else if ("COMMENTS".equalsIgnoreCase(columnName)) {
                        hasComments = true;
                    } else if ("TAG".equalsIgnoreCase(columnName)) {
                        hasTag = true;
                    } else if ("LIQUIBASE".equalsIgnoreCase(columnName)) {
                        hasLiquibase = true;
                    }
                }

                if (!hasDescription) {
                    statementsToExecute.add("ALTER TABLE DATABASECHANGELOG ADD DESCRIPTION VARCHAR(255)");
                }
                if (!hasTag) {
                    statementsToExecute.add("ALTER TABLE DATABASECHANGELOG ADD TAG VARCHAR(255)");
                }
                if (!hasComments) {
                    statementsToExecute.add("ALTER TABLE DATABASECHANGELOG ADD COMMENTS VARCHAR(255)");
                }
                if (!hasLiquibase) {
                    statementsToExecute.add("ALTER TABLE DATABASECHANGELOG ADD LIQUIBASE VARCHAR(255)");
                }

            } else {
                String createTableStatement = ("CREATE TABLE DATABASECHANGELOG (id varchar(255) not null, author varchar(255) not null, filename varchar(255) not null, dateExecuted " + getDateTimeType() + " not null, md5sum varchar(32), description varchar(255), comments varchar(255), tag varchar(255), liquibase varchar(10), primary key(id, author, filename))").toUpperCase();
                // If there is no table in the database for recording change history create one.
                statementsToExecute.add(createTableStatement);
                if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                    log.info("Creating database history table with name: DATABASECHANGELOG");
                } else {
                    changeLogTableExists = false;
                }
            }

            for (String sql : statementsToExecute) {
                if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                    statement = connection.createStatement();
                    statement.executeUpdate(sql);
                    connection.commit();
                } else {
                    if (!migrator.getMode().equals(Migrator.OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                        migrator.getOutputSQLWriter().append(sql + ";"+StreamUtil.getLineSeparator());
                        wroteToOutput = true;
                    }
                }
            }

            if (wroteToOutput) {
                migrator.getOutputSQLWriter().append(StreamUtil.getLineSeparator());
            }

        } finally {
            if (statement != null) {
                statement.close();
            }
            if (checkTableRS != null) {
                checkTableRS.close();
            }
            if (checkColumnsRS!= null) {
                checkColumnsRS.close();
            }
        }
    }

    public String getDatabaseChangeLogTableName() {
        return "DatabaseChangeLog".toUpperCase();
    }

    public String getCatalogName() throws SQLException {
        return null;
    }

    /**
     * This method will check the database ChangeLogLock table used to keep track of
     * if a machine is updating the database. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     *
     * @param migrator
     * @throws java.sql.SQLException
     */
    public void checkDatabaseChangeLogLockTable(Migrator migrator) throws SQLException, IOException {
        Statement statement = null;
        Connection connection = getConnection();
        ResultSet rs = null;
        changeLogLockTableExists = true;
        try {
            rs = connection.getMetaData().getTables(getCatalogName(), getSchemaName(), getDatabaseChangeLogLockTableName(), new String[]{"TABLE"});
            if (!rs.next()) {
                String createTableStatement = getCreateChangeLogLockSQL();
                // If there is no table in the database for recording change history create one.
                if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                    statement = connection.createStatement();
                    statement.executeUpdate(createTableStatement);
                    connection.commit();
                    log.info("Created database lock table with name: DATABASECHANGELOGLOCK");
                } else {
                    if (!migrator.getMode().equals(Migrator.OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                        if (!outputtedLockWarning) {
                            migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + "-----------------------------------------------------------------------------------------------"+StreamUtil.getLineSeparator());
                            migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " DATABASECHANGELOGLOCK table does not exist."+ StreamUtil.getLineSeparator());
                            migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " Race conditions may cause a corrupted sql script."+StreamUtil.getLineSeparator());
                            migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " Consider running: "+StreamUtil.getLineSeparator());
                            migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getCreateChangeLogLockSQL() + ";"+StreamUtil.getLineSeparator());
                            migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getChangeLogLockInsertSQL() + ";"+StreamUtil.getLineSeparator());
                            migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + "-----------------------------------------------------------------------------------------------"+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
                            outputtedLockWarning = true;
                        }

                        migrator.getOutputSQLWriter().append(createTableStatement + ";"+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
                    }
                    changeLogLockTableExists = false;
                }
            }
            rs.close();
            if (statement != null) {
                statement.close();
            }

            String insertRowStatment = getChangeLogLockInsertSQL();
            if (changeLogLockTableExists) {
                statement = connection.createStatement();

                rs = statement.executeQuery("select * from DATABASECHANGELOGLOCK where id=1".toUpperCase());
                if (!rs.next()) {
                    // If there is no table in the database for recording change history create one.
                    if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                        statement = connection.createStatement();
                        statement.executeUpdate(insertRowStatment);
                        connection.commit();
                        log.info("Created database lock table with name: DATABASECHANGELOGLOCK");
                    } else {
                        migrator.getOutputSQLWriter().append(insertRowStatment + ";"+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
                    }
                    rs.close();
                }
            } else {
                if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                    throw new SQLException("Change log lock table does not exist");
                } else {
                    migrator.getOutputSQLWriter().append(insertRowStatment + ";"+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
                }

            }

        } finally {
            if (statement != null) {
                statement.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }

    public String getDatabaseChangeLogLockTableName() {
        return "DatabaseChangeLogLock".toUpperCase();
    }

    private String getChangeLogLockInsertSQL() {
        return ("insert into DatabaseChangeLogLock (id, locked) values (1, " + getFalseBooleanValue() + ")").toUpperCase();
    }

    public String getFalseBooleanValue() {
        return "false";
    }

    public String getTrueBooleanValue() {
        return "true";
    }

    private String getCreateChangeLogLockSQL() {
        return ("create table DatabaseChangeLogLock (id int not null primary key, locked " + getBooleanType() + " not null, lockGranted " + getDateTimeType() + ", lockedby varchar(255))").toUpperCase();
    }


    public abstract boolean supportsInitiallyDeferrableColumns();

    public boolean doesChangeLogTableExist() {
        return changeLogTableExists;
    }

    public boolean doesChangeLogLockTableExist() {
        return changeLogLockTableExists;
    }

    public void dropDatabaseObjects() throws SQLException, MigrationFailedException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        try {

            dropForeignKeys(conn);
            dropTables(conn);

            if (this.supportsSequences()) {
                dropSequences(conn);
            }

            changeLogTableExists = false;
        } finally {
            if (conn != null) {
                conn.commit();
            }
        }
    }

    protected void dropForeignKeys(Connection conn) throws SQLException {
        //does nothing, assume tables will cascade constraints
    }

    protected ResultSet dropTables(Connection conn) throws SQLException, MigrationFailedException {
        //drop tables and their constraints
        ResultSet rs = null;
        Statement dropStatement = null;
        try {
            rs = conn.getMetaData().getTables(getCatalogName(), getSchemaName(), null, new String[]{"TABLE", "VIEW", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"});
            dropStatement = conn.createStatement();
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (tableName.startsWith("BIN$")) { //oracle deleted table
                    continue;
                } else if (tableName.startsWith("AQ$")) { //oracle AQ tables
                    continue;
                } else if (tableName.equalsIgnoreCase(getDatabaseChangeLogLockTableName())) {
                    continue;
                } else if (getSystemTablesAndViews().contains(tableName)) {
                    continue;
                }

                String type = rs.getString("TABLE_TYPE");
                String sql;
                if ("TABLE".equals(type)) {
                    sql = getDropTableSQL(tableName);
                } else if ("VIEW".equals(type)) {
                    sql = "DROP VIEW " + tableName;
                } else {
                    throw new MigrationFailedException("Unknown type " + type + " for " + tableName);
                }
                try {
                    log.finest("Dropping " + tableName);
                    dropStatement.executeUpdate(sql);
                } catch (SQLException e) {
                    throw new MigrationFailedException("Error dropping table '" + tableName + "': " + e.getMessage(), e);
                }
            }
            return rs;
        } finally {
            if (dropStatement != null) {
                dropStatement.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }

    protected Set<String> getSystemTablesAndViews() {
        return new HashSet<String>();
    }

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName + " CASCADE CONSTRAINTS";
    }

    protected void dropSequences(Connection conn) throws SQLException, MigrationFailedException {
        ; //no default
    }

    public abstract String getCurrentDateTimeFunction();

    public boolean aquireLock(Migrator migrator) throws MigrationFailedException {
        if (!migrator.getDatabase().doesChangeLogLockTableExist()) {
            if (migrator.getMode().equals(Migrator.EXECUTE_MODE) || migrator.getMode().equals(Migrator.EXECUTE_ROLLBACK_MODE))
            {
                throw new MigrationFailedException("Could not aquire lock, table does not exist");
            } else {
                return true;
            }
        }
        Connection conn = getConnection();
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(getSelectChangeLogLockSQL());
            if (!rs.next()) {
                throw new MigrationFailedException("Error checking database lock status");
            }
            boolean locked = rs.getBoolean(1);
            if (locked) {
                return false;
            } else {
                pstmt = conn.prepareStatement("update databasechangeloglock set locked=?, lockgranted=?, lockedby=? where id=1".toUpperCase());
                pstmt.setBoolean(1, true);
                pstmt.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
                pstmt.setString(3, InetAddress.getLocalHost().getCanonicalHostName() + " (" + InetAddress.getLocalHost().getHostAddress() + ")");
                if (pstmt.executeUpdate() != 1) {
                    throw new MigrationFailedException("Did not update change log lock correctly");
                }
                conn.commit();
                log.info("Successfully aquired change log lock");
                return true;
            }
        } catch (Exception e) {
            throw new MigrationFailedException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    ;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    ;
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    ;
                }
            }
        }

    }

    protected String getSelectChangeLogLockSQL() {
        return "select locked from databasechangeloglock where id=1".toUpperCase();
    }

    public String getLineComment() {
        return "--";
    }

    public void releaseLock(Migrator migrator) throws MigrationFailedException {
        if (doesChangeLogLockTableExist()) {
            Connection conn = getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement("update databasechangeloglock set locked=?, lockgranted=null, lockedby=null where id=1".toUpperCase());
                stmt.setBoolean(1, false);
                if (stmt.executeUpdate() != 1) {
                    throw new MigrationFailedException("Did not update change log lock correctly");
                }
                conn.commit();
                log.info("Successfully released change log lock");
            } catch (Exception e) {
                throw new MigrationFailedException(e);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        ;
                    }
                }
            }
        }
    }

    public DatabaseChangeLogLock[] listLocks(Migrator migrator) throws MigrationFailedException {
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select id, locked, lockgranted, lockedby from databasechangeloglock".toUpperCase());
            while (rs.next()) {
                boolean locked = rs.getBoolean("locked");
                if (locked) {
                    allLocks.add(new DatabaseChangeLogLock(rs.getInt("ID"), rs.getTimestamp("LOCKGRANTED"), rs.getString("LOCKEDBY")));
                }
            }
            return allLocks.toArray(new DatabaseChangeLogLock[allLocks.size()]);
        } catch (Exception e) {
            throw new MigrationFailedException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    ;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    ;
                }
            }
        }
    }

    public String getAutoIncrementClause() {
        return "AUTO_INCREMENT";
    }

    public abstract String getProductName();

    public void tag(String tagString) throws MigrationFailedException {
        Connection conn = getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(createChangeToTagSQL());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new MigrationFailedException("Did not tag database correctly");
            }
            Timestamp lastExecutedDate = rs.getTimestamp(1);
            rs.close();
            stmt.close();

            stmt = conn.prepareStatement(createTagSQL());
            stmt.setString(1, tagString);
            stmt.setTimestamp(2, lastExecutedDate);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new MigrationFailedException("Did not tag database change log correctly");
            }
            conn.commit();
        } catch (Exception e) {
            throw new MigrationFailedException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    ;
                }
            }
        }
    }

    protected String createChangeToTagSQL() {
        return "SELECT MAX(DATEEXECUTED) FROM " + getDatabaseChangeLogTableName() + "";
    }

    protected String createTagSQL() {
        return "UPDATE " + getDatabaseChangeLogTableName() + " SET TAG=? WHERE DATEEXECUTED=?";
    }

    public boolean doesTagExist(String tag) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = getConnection().prepareStatement("SELECT COUNT(*) FROM " + getDatabaseChangeLogTableName() + " WHERE TAG=?");
            pstmt.setString(1, tag);
            rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }
}