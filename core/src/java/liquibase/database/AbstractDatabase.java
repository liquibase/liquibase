package liquibase.database;

import liquibase.database.structure.DatabaseSnapshot;
import liquibase.DatabaseChangeLogLock;
import liquibase.migrator.Migrator;
import liquibase.change.ColumnConfig;
import liquibase.change.DropForeignKeyConstraintChange;
import liquibase.exception.JDBCException;
import liquibase.exception.LockException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * AbstractDatabase is extended by all supported databases as a facade to the underlying database.
 * The physical connection can be retrieved from the AbstractDatabase implementation, as well as any
 * database-specific characteristics such as the datatype for "boolean" fields.
 */
public abstract class AbstractDatabase implements Database {

    private DatabaseConnection connection;
    protected Logger log;
    protected boolean changeLogTableExists;
    protected boolean changeLogLockTableExists;
    protected boolean changeLogCreateAttempted;
    protected boolean changeLogLockCreateAttempted;

    private static boolean outputtedLockWarning = false;

    protected String currentDateTimeFunction;

    protected AbstractDatabase() {
        log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);
    }

    // ------- DATABASE INFORMATION METHODS ---- //

    public DatabaseConnection getConnection() {
        return connection;
    }

    public void setConnection(Connection conn) {
        this.connection = new SQLConnectionDelegate(conn);
        try {
            connection.setAutoCommit(getAutoCommitMode());
        } catch (SQLException sqle) {
            log.warning("Can not set auto commit to " + getAutoCommitMode() + " on connection");
        }
    }

    public void setConnection(DatabaseConnection conn) {
        this.connection = conn;
        try {
            connection.setAutoCommit(getAutoCommitMode());
        } catch (SQLException sqle) {
            log.warning("Can not set auto commit to " + getAutoCommitMode() + " on connection");
        }
    }

    /**
     * Auto-commit mode to run in
     */
    public boolean getAutoCommitMode() {
        return !supportsDDLInTransaction();
    }

    /**
     * By default databases should support DDL within a transaction.
     */
    public boolean supportsDDLInTransaction() {
        return true;
    }

    /**
     * Returns the name of the database product according to the underlying database.
     */
    public String getDatabaseProductName() {
        try {
            return connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot get database name");
        }
    }

    protected String getDatabaseProductName(Connection conn) throws JDBCException {
        try {
            return conn.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }


    public String getDatabaseProductVersion() throws JDBCException {
        try {
            return connection.getMetaData().getDatabaseProductVersion();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public String getDriverName() throws JDBCException {
        try {
            return connection.getMetaData().getDriverName();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public String getConnectionURL() throws JDBCException {
        try {
            return connection.getMetaData().getURL();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public String getConnectionUsername() throws JDBCException {
        try {
            return connection.getMetaData().getUserName();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public String getCatalogName() throws JDBCException {
        return null;
    }

    public String getSchemaName() throws JDBCException {
        return getConnectionUsername();
    }

    /**
     * Returns system (undroppable) tables and views.
     */
    protected Set<String> getSystemTablesAndViews() {
        return new HashSet<String>();
    }

    // ------- DATABASE FEATURE INFORMATION METHODS ---- //

    /**
     * Does the database type support sequence.
     */
    public boolean supportsSequences() {
        return true;
    }

    // ------- DATABASE-SPECIFIC SQL METHODS ---- //

    public void setCurrentDateTimeFunction(String function) {
        if (function != null) {
            this.currentDateTimeFunction = function;
        }
    }

    /**
     * Returns the database-specific datatype for the given column configuration.
     * This method will convert some generic column types (e.g. boolean, currency) to the correct type
     * for the current database.
     */
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
        } else if ("date".equalsIgnoreCase(column.getType())) {
            return getDateType();
        } else if ("time".equalsIgnoreCase(column.getType())) {
            return getTimeType();
        } else if ("dateTime".equalsIgnoreCase(column.getType())) {
            return getDateTimeType();
        } else {
            return column.getType();
        }
    }

    /**
     * Returns the actual database-specific data type to use a "boolean" column.
     */
    protected abstract String getBooleanType();

    /**
     * The database-specific value to use for "false" "boolean" columns.
     */
    public String getFalseBooleanValue() {
        return "false";
    }

    /**
     * The database-specific value to use for "true" "boolean" columns.
     */
    public String getTrueBooleanValue() {
        return "true";
    }

    /**
     * Return a date literal with the same value as a string formatted using ISO 8601.
     * <p/>
     * Note: many databases accept date literals in ISO8601 format with the 'T' replaced with
     * a space. Only databases which do not accept these strings should need to override this
     * method.
     * <p/>
     * Implementation restriction:
     * Currently, only the following subsets of ISO8601 are supported:
     * YYYY-MM-DD
     * hh:mm:ss
     * YYYY-MM-DDThh:mm:ss
     */
    public String getDateLiteral(String isoDate) {
        if (isDateOnly(isoDate) || isTimeOnly(isoDate)) {
            return "'" + isoDate + "'";
        } else if (isDateTime(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("'");
            val.append(isoDate.substring(0, 10));
            val.append(" ");
            //noinspection MagicNumber
            val.append(isoDate.substring(11));
            val.append("'");
            return val.toString();
        } else {
            return "BAD_DATE_FORMAT:" + isoDate;
        }
    }

    protected boolean isDateOnly(String isoDate) {
        return isoDate.length() == "YYYY-MM-DD".length();
    }

    protected boolean isDateTime(String isoDate) {
        return isoDate.length() == "YYYY-MM-DDThh:mm:ss".length();
    }

    protected boolean isTimeOnly(String isoDate) {
        return isoDate.length() == "hh:mm:ss".length();
    }

    /**
     * Returns the actual database-specific data type to use a "currency" column.
     */
    protected abstract String getCurrencyType();

    /**
     * Returns the actual database-specific data type to use a "UUID" column.
     */
    protected abstract String getUUIDType();

    /**
     * Returns the actual database-specific data type to use a "CLOB" column.
     */
    protected abstract String getClobType();

    /**
     * Returns the actual database-specific data type to use a "BLOB" column.
     */
    protected abstract String getBlobType();

    /**
     * Returns the actual database-specific data type to use a "date" (no time information) column.
     */
    protected String getDateType() {
        return "DATE";
    }

    /**
     * Returns the actual database-specific data type to use a "datetime" column.
     */
    protected abstract String getDateTimeType();

    /**
     * Returns the actual database-specific data type to use a "time" column.
     */
    protected String getTimeType() {
        return "TIME";
    }

    /**
     * Returns database-specific line comment string.
     */
    public String getLineComment() {
        return "--";
    }

    /**
     * Returns database-specific auto-increment DDL clause.
     */
    public String getAutoIncrementClause() {
        return "AUTO_INCREMENT";
    }

    /**
     * Returns database-specific commit command.
     */
    public String getCommitSQL() {
        return "COMMIT";
    }

    public String getConcatSql(String... values) {
        StringBuffer returnString = new StringBuffer();
        for (String value : values) {
            returnString.append(value).append(" || ");
        }

        return returnString.toString().replaceFirst(" \\|\\| $", "");
    }

    // ------- DATABASECHANGELOG / DATABASECHANGELOGLOCK METHODS ---- //

    public String getDatabaseChangeLogTableName() {
        return "DatabaseChangeLog".toUpperCase();
    }

    public String getDatabaseChangeLogLockTableName() {
        return "DatabaseChangeLogLock".toUpperCase();
    }

    private String getChangeLogLockInsertSQL() {
        return ("insert into " + getDatabaseChangeLogLockTableName() + " (id, locked) values (1, " + getFalseBooleanValue() + ")").toUpperCase();
    }

    protected String getCreateChangeLogLockSQL() {
        return ("create table " + getDatabaseChangeLogLockTableName() + " (id int not null primary key, locked " + getBooleanType() + " not null, lockGranted " + getDateTimeType() + ", lockedby varchar(255))").toUpperCase();
    }

    protected String getCreateChangeLogSQL() {
        return ("CREATE TABLE " + getDatabaseChangeLogTableName() + " (id varchar(150) not null, " +
                "author varchar(150) not null, " +
                "filename varchar(255) not null, " +
                "dateExecuted " + getDateTimeType() + " not null, " +
                "md5sum varchar(32), " +
                "description varchar(255), " +
                "comments varchar(255), " +
                "tag varchar(255), " +
                "liquibase varchar(10), " +
                "primary key(id, author, filename))").toUpperCase();
    }

    public boolean acquireLock(Migrator migrator) throws LockException {
        if (!migrator.getDatabase().doesChangeLogLockTableExist()) {
            if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE) || migrator.getMode().equals(Migrator.Mode.EXECUTE_ROLLBACK_MODE)) {
                throw new LockException("Could not acquire lock, table does not exist");
            } else {
                return true;
            }
        }
        DatabaseConnection conn = getConnection();
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(getSelectChangeLogLockSQL());
            if (!rs.next()) {
                throw new LockException("Error checking database lock status");
            }
            boolean locked = rs.getBoolean(1);
            if (locked) {
                return false;
            } else {
                pstmt = conn.prepareStatement("update " + getDatabaseChangeLogLockTableName() + " set locked=?, lockgranted=?, lockedby=? where id=1".toUpperCase());
                pstmt.setBoolean(1, true);
                pstmt.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
                pstmt.setString(3, InetAddress.getLocalHost().getCanonicalHostName() + " (" + InetAddress.getLocalHost().getHostAddress() + ")");
                if (pstmt.executeUpdate() != 1) {
                    throw new LockException("Did not update change log lock correctly");
                }
                conn.commit();
                log.info("Successfully acquired change log lock");
                return true;
            }
        } catch (Exception e) {
            throw new LockException(e);
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

    public void releaseLock() throws LockException {
        if (doesChangeLogLockTableExist()) {
            DatabaseConnection conn = getConnection();
            PreparedStatement stmt = null;
            try {
                String releaseSQL = ("update " + getDatabaseChangeLogLockTableName() + " set locked=?, lockgranted=null, lockedby=null where id=1").toUpperCase();
                stmt = conn.prepareStatement(releaseSQL);
                stmt.setBoolean(1, false);
                int updatedRows = stmt.executeUpdate();
                if (updatedRows != 1) {
                    throw new LockException("Did not update change log lock correctly.\n\n" + releaseSQL + " updated " + updatedRows + " instead of the expected 1 row.");
                }
                conn.commit();
                log.info("Successfully released change log lock");
            } catch (Exception e) {
                throw new LockException(e);
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

    public DatabaseChangeLogLock[] listLocks() throws LockException {
        DatabaseConnection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(("select id, locked, lockgranted, lockedby from " + getDatabaseChangeLogLockTableName()).toUpperCase());
            while (rs.next()) {
                boolean locked = rs.getBoolean("locked");
                if (locked) {
                    allLocks.add(new DatabaseChangeLogLock(rs.getInt("ID"), rs.getTimestamp("LOCKGRANTED"), rs.getString("LOCKEDBY")));
                }
            }
            return allLocks.toArray(new DatabaseChangeLogLock[allLocks.size()]);
        } catch (Exception e) {
            throw new LockException(e);
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

    protected String getSelectChangeLogLockSQL() {
        return ("select locked from " + getDatabaseChangeLogLockTableName() + " where id=1").toUpperCase();
    }

    public boolean doesChangeLogTableExist() {
        return changeLogTableExists;
    }

    public boolean doesChangeLogLockTableExist() {
        return changeLogLockTableExists;
    }

    /**
     * This method will check the database ChangeLog table used to keep track of
     * the changes in the file. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     */
    public void checkDatabaseChangeLogTable(Migrator migrator) throws JDBCException, IOException {
        Statement statement = null;
        DatabaseConnection connection = getConnection();
        ResultSet checkTableRS = null;
        ResultSet checkColumnsRS = null;
        List<String> statementsToExecute = new ArrayList<String>();
        boolean wroteToOutput = false;

        try {
            checkTableRS = connection.getMetaData().getTables(getCatalogName(), getSchemaName(), getDatabaseChangeLogTableName(), new String[]{"TABLE"});
            if (checkTableRS.next()) {
                changeLogTableExists = true;
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
                    statementsToExecute.add("ALTER TABLE " + getDatabaseChangeLogTableName() + " ADD DESCRIPTION VARCHAR(255)");
                }
                if (!hasTag) {
                    statementsToExecute.add("ALTER TABLE " + getDatabaseChangeLogTableName() + " ADD TAG VARCHAR(255)");
                }
                if (!hasComments) {
                    statementsToExecute.add("ALTER TABLE " + getDatabaseChangeLogTableName() + " ADD COMMENTS VARCHAR(255)");
                }
                if (!hasLiquibase) {
                    statementsToExecute.add("ALTER TABLE " + getDatabaseChangeLogTableName() + " ADD LIQUIBASE VARCHAR(255)");
                }

            } else if (!changeLogCreateAttempted) {
                changeLogCreateAttempted = true;
                String createTableStatement = getCreateChangeLogSQL();
                if (!canCreateChangeLogTable()) {
                    throw new JDBCException("Cannot create " + getDatabaseChangeLogTableName() + " table for your database.\n\n" +
                            "Please construct it manually using the following SQL as a base and re-run LiquiBase:\n\n" +
                            createTableStatement);
                }
                // If there is no table in the database for recording change history create one.
                statementsToExecute.add(createTableStatement);
                if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                    log.info("Creating database history table with name: " + getDatabaseChangeLogTableName());
                    changeLogTableExists = true;
                }
            }

            for (String sql : statementsToExecute) {
                if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                    statement = connection.createStatement();
                    statement.executeUpdate(sql);
                    connection.commit();
                } else {
                    if (!migrator.getMode().equals(Migrator.Mode.OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                        Writer writer = migrator.getOutputSQLWriter();
                        if (writer == null) {
                            wroteToOutput = false;
                        } else {
                            writer.append(sql).append(";").append(StreamUtil.getLineSeparator());
                            wroteToOutput = true;
                        }
                    }
                }
            }

            if (wroteToOutput) {
                migrator.getOutputSQLWriter().append(StreamUtil.getLineSeparator());
            }

        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
            if (checkTableRS != null) {
                try {
                    checkTableRS.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
            if (checkColumnsRS != null) {
                try {
                    checkColumnsRS.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
        }
    }

    protected boolean canCreateChangeLogTable() throws JDBCException {
        return true;
    }

    /**
     * This method will check the database ChangeLogLock table used to keep track of
     * if a machine is updating the database. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     */
    public void checkDatabaseChangeLogLockTable(Migrator migrator) throws JDBCException, IOException {
        Statement statement = null;
        DatabaseConnection connection = getConnection();
        ResultSet rs = null;
        try {
            rs = connection.getMetaData().getTables(getCatalogName(), getSchemaName(), getDatabaseChangeLogLockTableName(), new String[]{"TABLE"});
            if (!rs.next()) {
                if (!changeLogLockCreateAttempted) {
                    changeLogLockCreateAttempted = true;
                    String createTableStatement = getCreateChangeLogLockSQL();
                    // If there is no table in the database for recording change history create one.
                    if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                        statement = connection.createStatement();
                        statement.executeUpdate(createTableStatement);
                        connection.commit();
                        log.info("Created database lock table with name: " + getDatabaseChangeLogLockTableName());
                        changeLogLockTableExists = true;
                    } else {
                        if (!migrator.getMode().equals(Migrator.Mode.OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                            if (!outputtedLockWarning) {
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + "-----------------------------------------------------------------------------------------------" + StreamUtil.getLineSeparator());
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getDatabaseChangeLogLockTableName() + " table does not exist." + StreamUtil.getLineSeparator());
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " Race conditions may cause a corrupted sql script." + StreamUtil.getLineSeparator());
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " Consider running: " + StreamUtil.getLineSeparator());
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getCreateChangeLogLockSQL() + ";" + StreamUtil.getLineSeparator());
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getChangeLogLockInsertSQL() + ";" + StreamUtil.getLineSeparator());
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + "-----------------------------------------------------------------------------------------------" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
                                outputtedLockWarning = true;
                            }

                            migrator.getOutputSQLWriter().append(createTableStatement);
                            migrator.getOutputSQLWriter().append(";");
                            migrator.getOutputSQLWriter().append(StreamUtil.getLineSeparator());
                            migrator.getOutputSQLWriter().append(StreamUtil.getLineSeparator());
                        }
                    }
                }
            } else {
                changeLogLockTableExists = true;
            }
            rs.close();
            if (statement != null) {
                statement.close();
            }

            String insertRowStatment = getChangeLogLockInsertSQL();
            if (changeLogLockTableExists) {
                statement = connection.createStatement();

                rs = statement.executeQuery("select * from " + getDatabaseChangeLogLockTableName() + " where id=1".toUpperCase());
                if (!rs.next()) {
                    // If there is no table in the database for recording change history create one.
                    if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                        statement = connection.createStatement();
                        statement.executeUpdate(insertRowStatment);
                        connection.commit();
                        log.info("Created database lock table with name: " + getDatabaseChangeLogLockTableName());
                    } else {
                        migrator.getOutputSQLWriter().append(insertRowStatment);
                        migrator.getOutputSQLWriter().append(";");
                        migrator.getOutputSQLWriter().append(StreamUtil.getLineSeparator());
                        migrator.getOutputSQLWriter().append(StreamUtil.getLineSeparator());
                    }
                    rs.close();
                }
            } else {
                if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                    throw new JDBCException("Change log lock table does not exist");
                }
            }

        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
        }
    }

    // ------- DATABASE OBJECT DROPPING METHODS ---- //

    /**
     * Drops all objects owned by the connected user.
     */
    public void dropDatabaseObjects() throws JDBCException {
        DatabaseConnection conn = getConnection();
        try {
            dropForeignKeys(conn);
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
                //noinspection ThrowFromFinallyBlock
                throw new JDBCException(e);
            }
        }
    }

    protected void dropForeignKeys(DatabaseConnection conn) throws JDBCException {
        ResultSet tableRS;
        ResultSet fkRS = null;
        Statement dropStatement = null;
        try {
            tableRS = conn.getMetaData().getTables(getCatalogName(), getSchemaName(), null, getTableTypes());
            while (tableRS.next()) {
                String tableName = tableRS.getString("TABLE_NAME");
                String schemaName = tableRS.getString("TABLE_SCHEM");
                String catalogName = tableRS.getString("TABLE_CAT");
                if (isSystemTable(catalogName, schemaName, tableName)) {
                    continue;
                }

                fkRS = conn.getMetaData().getExportedKeys(getCatalogName(), getSchemaName(), tableName);
                dropStatement = conn.createStatement();
                while (fkRS.next()) {
                    DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
                    dropFK.setBaseTableName(fkRS.getString("FKTABLE_NAME"));
                    dropFK.setConstraintName(fkRS.getString("FK_NAME"));

                    try {
                        dropStatement.execute(dropFK.generateStatements(this)[0].getSqlStatement(this));
                    } catch (UnsupportedChangeException e) {
                        throw new JDBCException(e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            if (dropStatement != null) {
                try {
                    dropStatement.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
            if (fkRS != null) {
                try {
                    fkRS.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
        }
    }

    private String[] getTableTypes() throws JDBCException {
        List<String> wantedTypes = new ArrayList<String>(Arrays.asList("TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"));
        List<String> availableTypes = new ArrayList<String>();

        try {
            ResultSet types = connection.getMetaData().getTableTypes();
            while (types.next()) {
                availableTypes.add(types.getString("TABLE_TYPE").trim());
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        }

        List<String> returnTypes = new ArrayList<String>();
        for (String type : wantedTypes) {
            if (availableTypes.contains(type)) {
                returnTypes.add(type);
            }
        }

        return returnTypes.toArray(new String[returnTypes.size()]);

    }

    protected void dropTables(DatabaseConnection conn) throws JDBCException {
        //drop tables and their constraints
        ResultSet rs = null;
        Statement dropStatement = null;
        try {
            rs = conn.getMetaData().getTables(getCatalogName(), getSchemaName(), null, getTableTypes());
            dropStatement = conn.createStatement();
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String schemaName = rs.getString("TABLE_SCHEM");
                String catalogName = rs.getString("TABLE_CAT");
                if (isSystemTable(catalogName, schemaName, tableName)) {
                    continue;
                }

                String type = rs.getString("TABLE_TYPE");
                String sql;
                if ("TABLE".equals(type)) {
                    sql = getDropTableSQL(tableName);
                } else if ("VIEW".equals(type)) {
                    sql = "DROP VIEW " + tableName;
                } else if ("SYSTEM TABLE".equals(type)) {
                    continue; //don't drop it
                } else {
                    throw new JDBCException("Unknown type " + type + " for " + tableName);
                }
                try {
                    log.finest("Dropping " + tableName);
                    dropStatement.executeUpdate(sql);
                } catch (SQLException e) {
                    throw new JDBCException("Error dropping table '" + tableName + "': " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            if (dropStatement != null) {
                try {
                    dropStatement.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
        }
    }

    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        if ("information_schema".equalsIgnoreCase(schemaName)) {
            return true;
        } else if (tableName.equalsIgnoreCase(getDatabaseChangeLogLockTableName())) {
            return true;
        } else if (getSystemTablesAndViews().contains(tableName)) {
            return true;
        }
        return false;
    }

    public boolean isLiquibaseTable(String tableName) {
        return tableName.equalsIgnoreCase(this.getDatabaseChangeLogTableName()) || tableName.equalsIgnoreCase(this.getDatabaseChangeLogLockTableName());
    }


    protected void dropViews(DatabaseConnection conn) throws JDBCException {
        //drop tables and their constraints
        ResultSet rs = null;
        Statement dropStatement = null;
        try {
            rs = conn.getMetaData().getTables(getCatalogName(), getSchemaName(), null, new String[]{"VIEW"});
            dropStatement = conn.createStatement();
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (getSystemTablesAndViews().contains(tableName)) {
                    continue;
                }

                String sql = "DROP VIEW " + tableName;

                try {
                    log.finest("Dropping view " + tableName);
                    dropStatement.executeUpdate(sql);
                } catch (SQLException e) {
                    throw new JDBCException("Error dropping view '" + tableName + "': " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            if (dropStatement != null) {
                try {
                    dropStatement.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
        }
    }

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName;
    }

    abstract protected void dropSequences(DatabaseConnection conn) throws JDBCException;

    // ------- DATABASE TAGGING METHODS ---- //

    /**
     * Tags the database changelog with the given string.
     */
    public void tag(String tagString) throws JDBCException {
        DatabaseConnection conn = getConnection();
        PreparedStatement stmt = null;
        Statement countStatement = null;
        ResultSet countRS;
        try {
            stmt = conn.prepareStatement(createChangeToTagSQL());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new JDBCException("Did not tag database correctly");
            }
            Timestamp lastExecutedDate = rs.getTimestamp(1);
            rs.close();
            stmt.close();

            stmt = conn.prepareStatement(createTagSQL());
            stmt.setString(1, tagString);
            stmt.setTimestamp(2, lastExecutedDate);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                countStatement = conn.createStatement();
                countRS = countStatement.executeQuery("select count(*) from " + getDatabaseChangeLogTableName());
                countRS.next();
                if (countRS.getInt(1) == 0) {
                    throw new JDBCException("Cannot tag an empty database");
                }
                countRS.close();

                throw new JDBCException("Did not tag database change log correctly");
            }
            conn.commit();
        } catch (Exception e) {
            throw new JDBCException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    ;
                }
            }
            if (countStatement != null) {
                try {
                    countStatement.close();
                } catch (SQLException e) {
                    ;
                }
            }
        }
    }

    /**
     * Returns SQL to return the date of the most recient changeset execution.
     */
    protected String createChangeToTagSQL() {
        return "SELECT MAX(DATEEXECUTED) FROM " + getDatabaseChangeLogTableName() + "";
    }

    /**
     * Returns SQL to tag the database.  SQL Contains two ?:
     * <ol>
     * <li>tag string</li>
     * <li>date executed</li>
     * </ol>
     */
    protected String createTagSQL() {
        return "UPDATE " + getDatabaseChangeLogTableName() + " SET TAG=? WHERE DATEEXECUTED=?";
    }

    public String createFindSequencesSQL() throws JDBCException {
        return null;
    }

    public boolean doesTagExist(String tag) throws JDBCException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = getConnection().prepareStatement("SELECT COUNT(*) FROM " + getDatabaseChangeLogTableName() + " WHERE TAG=?");
            pstmt.setString(1, tag);
            rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new JDBCException(e);
                }
            }
        }
    }

    public DatabaseSnapshot getSnapshot() throws JDBCException {
        return new DatabaseSnapshot(this);
    }


    public String toString() {
        try {
            return getConnectionUsername() + " @ " + getConnectionURL();
        } catch (JDBCException e) {
            return super.toString();
        }
    }


    public boolean shouldQuoteValue(String value) {
        return true;
    }

    public String getViewDefinition(String name) throws JDBCException {
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            String definition;
            stmt = getConnection().createStatement();
            resultSet = stmt.executeQuery(getViewDefinitionSql(name));
            if (resultSet.next()) {
                definition = resultSet.getString(1);
            } else {
                throw new JDBCException("Could not retrieve definition for view " + name);
            }
            if (resultSet.next()) {
                throw new JDBCException("Found too many definitions for " + name);
            }
            return definition;
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
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

    protected String getViewDefinitionSql(String name) throws JDBCException {
        String sql = "select view_definition from information_schema.views where upper(table_name)='" + name.toUpperCase() + "'";
        if (getSchemaName() != null) {
            sql += " and table_schema='" + getSchemaName() + "'";
        }
        if (getCatalogName() != null) {
            sql +=  " and table_catalog='"+getCatalogName()+"'";

        }
        return sql;
    }

}
