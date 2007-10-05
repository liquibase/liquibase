package liquibase.database;

import liquibase.DatabaseChangeLogLock;
import liquibase.change.*;
import liquibase.database.sql.PreparedSqlStatement;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.UpdateStatement;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.exception.LockException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.migrator.Migrator;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * AbstractDatabase is extended by all supported databases as a facade to the underlying database.
 * The physical connection can be retrieved from the AbstractDatabase implementation, as well as any
 * database-specific characteristics such as the datatype for "boolean" fields.
 */
public abstract class AbstractDatabase implements Database {

    private DatabaseConnection connection;
    static final protected Logger log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);
    protected boolean changeLogTableExists;
    protected boolean changeLogLockTableExists;
    protected boolean changeLogCreateAttempted;
    protected boolean changeLogLockCreateAttempted;

    private static boolean outputtedLockWarning = false;

    protected String currentDateTimeFunction;

    protected AbstractDatabase() {
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
        } else if ("BLOB".equalsIgnoreCase(column.getType())
                || "LONGVARBINARY".equalsIgnoreCase(column.getType())) {
            return getBlobType();
        } else if ("CLOB".equalsIgnoreCase(column.getType())
                || "TEXT".equalsIgnoreCase(column.getType())
                || "LONGVARCHAR".equalsIgnoreCase(column.getType())) {
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
    public SqlStatement getCommitSQL() {
        return new RawSqlStatement("COMMIT");
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

    private SqlStatement getChangeLogLockInsertSQL() {
        return new RawSqlStatement(("insert into " + getDatabaseChangeLogLockTableName() + " (id, locked) values (1, " + getFalseBooleanValue() + ")").toUpperCase());
    }

    protected SqlStatement getCreateChangeLogLockSQL() {
        return new RawSqlStatement(("create table " + getDatabaseChangeLogLockTableName() + " (id int not null primary key, locked " + getBooleanType() + " not null, lockGranted " + getDateTimeType() + ", lockedby varchar(255))").toUpperCase());
    }

    protected SqlStatement getCreateChangeLogSQL() {
        return new RawSqlStatement(("CREATE TABLE " + getDatabaseChangeLogTableName() + " (id varchar(150) not null, " +
                "author varchar(150) not null, " +
                "filename varchar(255) not null, " +
                "dateExecuted " + getDateTimeType() + " not null, " +
                "md5sum varchar(32), " +
                "description varchar(255), " +
                "comments varchar(255), " +
                "tag varchar(255), " +
                "liquibase varchar(10), " +
                "primary key(id, author, filename))").toUpperCase());
    }

    public boolean acquireLock(Migrator migrator) throws LockException {
        if (!migrator.getDatabase().doesChangeLogLockTableExist()) {
            if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE) || migrator.getMode().equals(Migrator.Mode.EXECUTE_ROLLBACK_MODE)) {
                throw new LockException("Could not acquire lock, table does not exist");
            } else {
                return true;
            }
        }

        try {
            Boolean locked = null;
            try {
                locked = (Boolean) new JdbcTemplate(this).queryForObject(getSelectChangeLogLockSQL(), Boolean.class);
            } catch (JDBCException e) {
                throw new LockException("Error checking database lock status", e);
            }
            if (locked) {
                return false;
            } else {
                UpdateStatement updateStatement = new UpdateStatement();
                updateStatement.setTableName(getDatabaseChangeLogLockTableName());
                updateStatement.addNewColumnValue("LOCKED", true, Types.BOOLEAN);
                updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()), Types.TIMESTAMP);
                updateStatement.addNewColumnValue("LOCKEDBY", InetAddress.getLocalHost().getCanonicalHostName() + " (" + InetAddress.getLocalHost().getHostAddress() + ")", Types.VARCHAR);
                updateStatement.setWhereClause("ID  = 1");

                if (new JdbcTemplate(this).update((PreparedSqlStatement) updateStatement) != 1) {
                    throw new LockException("Did not update change log lock correctly");
                }
                getConnection().commit();
                log.info("Successfully acquired change log lock");
                return true;
            }
        } catch (Exception e) {
            throw new LockException(e);
        }

    }

    public void releaseLock() throws LockException {
        if (doesChangeLogLockTableExist()) {
            try {
                UpdateStatement releaseStatement = new UpdateStatement();
                releaseStatement.setTableName(getDatabaseChangeLogLockTableName());
                releaseStatement.addNewColumnValue("LOCKED", false, Types.BOOLEAN);
                releaseStatement.addNewColumnValue("LOCKGRANTED", null, Types.TIMESTAMP);
                releaseStatement.addNewColumnValue("LOCKEDBY", null, Types.VARCHAR);
                releaseStatement.setWhereClause(" ID = 1");

                int updatedRows = new JdbcTemplate(this).update(releaseStatement);
                if (updatedRows != 1) {
                    throw new LockException("Did not update change log lock correctly.\n\n" + releaseStatement + " updated " + updatedRows + " instead of the expected 1 row.");
                }
                getConnection().commit();
                log.info("Successfully released change log lock");
            } catch (Exception e) {
                throw new LockException(e);
            }
        }
    }

    public DatabaseChangeLogLock[] listLocks() throws LockException {
        try {
            List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
            RawSqlStatement sqlStatement = new RawSqlStatement((("select id, locked, lockgranted, lockedby from " + getDatabaseChangeLogLockTableName()).toUpperCase()));
            List<Map> rows = new JdbcTemplate(this).queryForList(sqlStatement);
            for (Map columnMap : rows) {
                Boolean locked = (Boolean) columnMap.get("locked");
                if (locked) {
                    allLocks.add(new DatabaseChangeLogLock((Integer) columnMap.get("id"), (Date) columnMap.get("LOCKGRANTED"), (String) columnMap.get("LOCKEDBY")));
                }
            }
            return allLocks.toArray(new DatabaseChangeLogLock[allLocks.size()]);
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    protected SqlStatement getSelectChangeLogLockSQL() {
        return new RawSqlStatement(("select locked from " + getDatabaseChangeLogLockTableName() + " where id=1").toUpperCase());
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
        DatabaseConnection connection = getConnection();
        ResultSet checkTableRS = null;
        ResultSet checkColumnsRS = null;
        List<SqlStatement> statementsToExecute = new ArrayList<SqlStatement>();
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
                    statementsToExecute.add(new RawSqlStatement("ALTER TABLE " + getDatabaseChangeLogTableName() + " ADD DESCRIPTION VARCHAR(255)"));
                }
                if (!hasTag) {
                    statementsToExecute.add(new RawSqlStatement("ALTER TABLE " + getDatabaseChangeLogTableName() + " ADD TAG VARCHAR(255)"));
                }
                if (!hasComments) {
                    statementsToExecute.add(new RawSqlStatement("ALTER TABLE " + getDatabaseChangeLogTableName() + " ADD COMMENTS VARCHAR(255)"));
                }
                if (!hasLiquibase) {
                    statementsToExecute.add(new RawSqlStatement("ALTER TABLE " + getDatabaseChangeLogTableName() + " ADD LIQUIBASE VARCHAR(255)"));
                }

            } else if (!changeLogCreateAttempted) {
                changeLogCreateAttempted = true;
                SqlStatement createTableStatement = getCreateChangeLogSQL();
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

            for (SqlStatement sql : statementsToExecute) {
                if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                    new JdbcTemplate(this).update(sql);
                    connection.commit();
                } else {
                    if (!migrator.getMode().equals(Migrator.Mode.OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                        Writer writer = migrator.getOutputSQLWriter();
                        if (writer == null) {
                            wroteToOutput = false;
                        } else {
                            writer.append(sql.getSqlStatement(this)).append(sql.getEndDelimiter(this)).append(StreamUtil.getLineSeparator());
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
        DatabaseConnection connection = getConnection();
        ResultSet rs = null;
        try {
            rs = connection.getMetaData().getTables(getCatalogName(), getSchemaName(), getDatabaseChangeLogLockTableName(), new String[]{"TABLE"});
            if (!rs.next()) {
                if (!changeLogLockCreateAttempted) {
                    changeLogLockCreateAttempted = true;
                    SqlStatement createTableStatement = getCreateChangeLogLockSQL();
                    // If there is no table in the database for recording change history create one.
                    if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                        new JdbcTemplate(this).update(createTableStatement);
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
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getCreateChangeLogLockSQL().getSqlStatement(this) + ";" + StreamUtil.getLineSeparator());
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + " " + getChangeLogLockInsertSQL().getSqlStatement(this) + ";" + StreamUtil.getLineSeparator());
                                migrator.getOutputSQLWriter().write(migrator.getDatabase().getLineComment() + "-----------------------------------------------------------------------------------------------" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
                                outputtedLockWarning = true;
                            }

                            migrator.getOutputSQLWriter().append(createTableStatement.getSqlStatement(this));
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

            SqlStatement insertRowStatment = getChangeLogLockInsertSQL();
            if (changeLogLockTableExists) {
                RawSqlStatement selectStatement = new RawSqlStatement(("select count(*) from " + getDatabaseChangeLogLockTableName() + " where id=1".toUpperCase()));
                int rows = new JdbcTemplate(this).queryForInt(selectStatement);
                if (rows == 0) {
                    if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                        new JdbcTemplate(this).update(insertRowStatment);
                        connection.commit();
                        log.info("Inserted lock row into: " + getDatabaseChangeLogLockTableName());
                    } else {
                        migrator.getOutputSQLWriter().append(insertRowStatment.getSqlStatement(this));
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
                while (fkRS.next()) {
                    DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
                    dropFK.setBaseTableName(fkRS.getString("FKTABLE_NAME"));
                    dropFK.setConstraintName(fkRS.getString("FK_NAME"));

                    try {
                        new JdbcTemplate(this).execute(dropFK.generateStatements(this)[0]);
                    } catch (UnsupportedChangeException e) {
                        throw new JDBCException(e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
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
        try {
            rs = conn.getMetaData().getTables(getCatalogName(), getSchemaName(), null, getTableTypes());
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String schemaName = rs.getString("TABLE_SCHEM");
                String catalogName = rs.getString("TABLE_CAT");
                if (isSystemTable(catalogName, schemaName, tableName)) {
                    continue;
                }

                String type = rs.getString("TABLE_TYPE");
                Change dropChange;
                if ("TABLE".equals(type)) {
                    dropChange = new DropTableChange();
                    ((DropTableChange) dropChange).setTableName(tableName);
                } else if ("VIEW".equals(type)) {
                    dropChange = new DropViewChange();
                    ((DropViewChange) dropChange).setViewName(tableName);
                } else if ("SYSTEM TABLE".equals(type)) {
                    continue; //don't drop it
                } else {
                    throw new JDBCException("Unknown type " + type + " for " + tableName);
                }
                try {
                    log.finest("Dropping " + tableName);
                    dropChange.executeStatements(this);
                } catch (UnsupportedChangeException e) {
                    throw new JDBCException("Error dropping table '" + tableName + "': " + e.getMessage(), e);
                }
            }
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
        try {
            rs = conn.getMetaData().getTables(getCatalogName(), getSchemaName(), null, new String[]{"VIEW"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (getSystemTablesAndViews().contains(tableName)) {
                    continue;
                }

                DropViewChange dropChange = new DropViewChange();
                dropChange.setViewName(tableName);

                try {
                    log.finest("Dropping view " + tableName);
                    dropChange.executeStatements(this);
                } catch (UnsupportedChangeException e) {
                    throw new JDBCException("Error dropping view '" + tableName + "': " + e.getMessage(), e);
                }
            }
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
        }
    }

    protected void dropSequences(DatabaseConnection conn) throws JDBCException {
        //noinspection unchecked
        List<String> sequences = (List<String>) new JdbcTemplate(this).queryForList(createFindSequencesSQL(), String.class);
        for (String sequenceName : sequences) {
            log.finest("Dropping sequence " + sequenceName);
            DropSequenceChange dropChange = new DropSequenceChange();
            dropChange.setSequenceName(sequenceName);
            try {
                dropChange.executeStatements(this);
            } catch (UnsupportedChangeException e) {
                throw new JDBCException("Error dropping sequence '" + sequenceName + "': " + e.getMessage(), e);
            }
        }
    }

    // ------- DATABASE TAGGING METHODS ---- //

    /**
     * Tags the database changelog with the given string.
     */
    public void tag(String tagString) throws JDBCException {
        try {
            int totalRows = new JdbcTemplate(this).queryForInt(new RawSqlStatement("select count(*) from " + getDatabaseChangeLogTableName()));
            if (totalRows == 0) {
                throw new JDBCException("Cannot tag an empty database");
            }

            Date lastExecutedDate = (Date) new JdbcTemplate(this).queryForObject(createChangeToTagSQL(), Date.class);
            int rowsUpdated = new JdbcTemplate(this).update(createTagSQL(tagString, lastExecutedDate));
            if (rowsUpdated == 0) {

                throw new JDBCException("Did not tag database change log correctly");
            }
            getConnection().commit();
        } catch (Exception e) {
            throw new JDBCException(e);
        }
    }

    /**
     * Returns SQL to return the date of the most recient changeset execution.
     */
    protected SqlStatement createChangeToTagSQL() {
        return new RawSqlStatement("SELECT MAX(DATEEXECUTED) FROM " + getDatabaseChangeLogTableName());
    }

    /**
     * Returns SQL to tag the database.  SQL Contains two ?:
     * <ol>
     * <li>tag string</li>
     * <li>date executed</li>
     * </ol>
     */
    protected SqlStatement createTagSQL(String tagName, Date dateExecuted) {
        UpdateStatement statement = new UpdateStatement();
        statement.setTableName(getDatabaseChangeLogTableName());
        statement.addNewColumnValue("TAG", tagName, Types.VARCHAR);
        statement.setWhereClause("DATEEXECUTED = ?");
        statement.addWhereParameter(dateExecuted, Types.TIMESTAMP);

        return statement;
    }

    public SqlStatement createFindSequencesSQL() throws JDBCException {
        return null;
    }

    public boolean doesTagExist(String tag) throws JDBCException {
        int count = new JdbcTemplate(this).queryForInt(new RawSqlStatement("SELECT COUNT(*) FROM " + getDatabaseChangeLogTableName() + " WHERE TAG='" + tag + "'"));
        return count > 0;
    }

    public DatabaseSnapshot getSnapshot() throws JDBCException {
        return new DatabaseSnapshot(this);
    }


    public String toString() {
        if (getConnection() == null) {
            return getProductName()+" Database";
        }
        try {
            return getConnectionUsername() + " @ " + getConnectionURL();
        } catch (JDBCException e) {
            return super.toString();
        }
    }


    public boolean shouldQuoteValue(String value) {
        return true;
    }

    public String getViewDefinition(String viewName) throws JDBCException {
        return (String) new JdbcTemplate(this).queryForObject(getViewDefinitionSql(viewName), String.class);
    }

    protected SqlStatement getViewDefinitionSql(String viewName) throws JDBCException {
        String sql = "select view_definition from information_schema.views where upper(table_name)='" + viewName.toUpperCase() + "'";
        if (getSchemaName() != null) {
            sql += " and table_schema='" + getSchemaName() + "'";
        }
        if (getCatalogName() != null) {
            sql += " and table_catalog='" + getCatalogName() + "'";

        }
//        log.info("GetViewDefinitionSQL: "+sql);
        return new RawSqlStatement(sql);
    }


    public int getDatabaseType(int type) {
        int returnType = type;
        if (returnType == Types.BOOLEAN) {
            String booleanType = getBooleanType();
            if (!booleanType.equalsIgnoreCase("boolean")) {
                returnType = Types.TINYINT;
            }
        }

        return returnType;
    }
}
