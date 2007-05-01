package liquibase.database;

import liquibase.migrator.MigrationFailedException;
import liquibase.migrator.Migrator;
import liquibase.migrator.DatabaseChangeLogLock;
import liquibase.migrator.change.ColumnConfig;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.net.InetAddress;

import com.sun.media.jai.util.CaselessStringArrayTable;

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
        ResultSet rs = null;
        changeLogTableExists = true;
        try {
            rs = connection.getMetaData().getTables(getCatalogName(), getSchemaName(), getDatabaseChangeLogTableName(), new String[]{"TABLE"});
            if (!rs.next()) {
                String createTableStatement = ("create table DATABASECHANGELOG (id varchar(255) not null, author varchar(255) not null, filename varchar(255) not null, dateExecuted " + getDateTimeType() + " not null, md5sum varchar(32), primary key(id, author, filename))").toUpperCase();
                // If there is no table in the database for recording change history create one.
                if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                    statement = connection.createStatement();
                    statement.executeUpdate(createTableStatement);
                    connection.commit();
                    log.info("Created database history table with name: DATABASECHANGELOG");
                } else {
                    migrator.getOutputSQLWriter().append(createTableStatement + ";\n\n");
                    changeLogTableExists = false;
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
                    migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + "-----------------------------------------------------------------------------------------------\n");
                    migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " DATABASECHANGELOGLOCK table does not exist.\n");
                    migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " Race conditions may cause a corrupted sql script.\n");
                    migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " Consider running: \n");
                    migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getCreateChangeLogLockSQL() + ";\n");
                    migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getChangeLogLockInsertSQL() + ";\n");
                    migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + "-----------------------------------------------------------------------------------------------\n\n");

                    migrator.getOutputSQLWriter().append(createTableStatement + ";\n\n");
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
                        migrator.getOutputSQLWriter().append(insertRowStatment + ";\n\n");
                    }
                    rs.close();
                }
            } else {
                if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                    throw new SQLException("Change log lock table does not exist");
                } else {
                    migrator.getOutputSQLWriter().append(insertRowStatment + ";\n\n");
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
        Statement selectStatement = null;
        Statement dropStatement = null;

        ResultSet rs = null;
        try {
            selectStatement = conn.createStatement();
            dropStatement = conn.createStatement();

            //drop tables and their constraints
            rs = conn.getMetaData().getTables(getCatalogName(), getSchemaName(), null, new String[]{"TABLE", "VIEW", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (tableName.startsWith("BIN$")) { //oracle deleted table
                    continue;
                } else if (tableName.startsWith("AQ$")) { //oracle AQ tables
                    continue;
                } else if (tableName.equals(getDatabaseChangeLogLockTableName())) {
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
            rs.close();

            if (this.supportsSequences()) {
                dropSequences(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (selectStatement != null) {
                selectStatement.close();
            }
            if (dropStatement != null) {
                dropStatement.close();
            }
            if (conn != null) {
                conn.commit();
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

    public String getRenameColumnSQL(String tableName, String oldColumnName, String newColumnName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table ").append(tableName);
        buffer.append(" rename column ");
        buffer.append(oldColumnName).append(" ");
        buffer.append(" to ").append(newColumnName);
        return buffer.toString();
    }

    public String getRenameTableSQL(String oldTableName, String newTableName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("rename ").append(oldTableName).append(" to ").append(newTableName);
        return buffer.toString();
    }

    public String getDropIndexSQL(String tableName, String indexName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP INDEX ");
        buffer.append(indexName);
        buffer.append(" ON ");
        buffer.append(tableName);
        return buffer.toString();
    }

    public String getDropNullConstraintSQL(String tableName, String columnName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("alter table ");
        buffer.append(tableName);
        buffer.append(" alter column  ");
        buffer.append(columnName);
        buffer.append(" ").append(getColumnDataType(tableName, columnName));
        buffer.append(" null");
        return buffer.toString();
    }

    public String getAddNullConstraintSQL(String tableName, String columnName, String defaultNullValue) {
        StringBuffer buffer = new StringBuffer();
        try {
            String columnType = this.getColumnDataType(tableName, columnName);
            this.updateNullColumns(tableName, columnName, defaultNullValue);
            buffer.append("alter table ").append(tableName);
            buffer.append(" alter column ");
            buffer.append(columnName).append(" ");
            buffer.append(columnType).append(" ");
            buffer.append("not null");
        } catch (SQLException eSqlException) {
            throw new RuntimeException(eSqlException);
        }
        return buffer.toString();
    }

    public boolean aquireLock(Migrator migrator) throws MigrationFailedException {
        if (!migrator.getDatabase().doesChangeLogLockTableExist()) {
            if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
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

    public String getColumnDataType(String tableName, String columnName) {
        ResultSet rs = null;
        Statement selectStatement = null;
        Connection connection = getConnection();
        ResultSetMetaData rsdata;
        int columnCount;
        String columnType = "";
        try {
            selectStatement = connection.createStatement();
            rs = selectStatement.executeQuery("SELECT * FROM " + tableName);  //todo: is there a more efficient way to do this?

            rsdata = rs.getMetaData();
            columnCount = rsdata.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {

                if (columnName.equals(rsdata.getColumnName(i))) {

                    columnType = rsdata.getColumnTypeName(i) + "(" + rsdata.getColumnDisplaySize(i) + ")";
                    break;
                }


            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (selectStatement != null) {
                try {
                    selectStatement.close();
                } catch (SQLException e) {
                    ;
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    ;
                }
            }
        }

        return columnType;
    }

    public void updateNullColumns(String tableName, String columnName, String defalutValue) throws SQLException {
        Statement updateStatement = null;
        Connection connection = getConnection();

        try {
            updateStatement = connection.createStatement();
            updateStatement.executeUpdate("update " + tableName + " set " + columnName + "='" + defalutValue + "' where " + columnName + " is NULL");
        } finally {
            if (updateStatement != null) {
                updateStatement.close();
            }
        }
    }

    public String getCreateSequenceSQL(String sequenceName, Integer startValue, Integer incrementBy, Integer minValue, Integer maxValue, Boolean ordered) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE SEQUENCE ");
        buffer.append(sequenceName);
        if (startValue != null) {
            buffer.append(" START WITH ").append(startValue);
        }
        if (incrementBy != null) {
            buffer.append(" INCREMENT BY ").append(incrementBy);
        }
        if (minValue != null) {
            buffer.append(" MINVALUE ").append(minValue);
        }
        if (maxValue != null) {
            buffer.append(" MAXVALUE ").append(maxValue);
        }

        return buffer.toString().trim();
    }

    public String getAlterSequenceSQL(String sequenceName, Integer incrementBy, Integer minValue, Integer maxValue, Boolean ordered) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ALTER SEQUENCE ");
        buffer.append(sequenceName);

        if (incrementBy != null) {
            buffer.append(" INCREMENT BY ").append(incrementBy);
        }
        if (minValue != null) {
            buffer.append(" MINVALUE ").append(minValue);
        }
        if (maxValue != null) {
            buffer.append(" MAXVALUE ").append(maxValue);
        }

        return buffer.toString().trim();
    }

}