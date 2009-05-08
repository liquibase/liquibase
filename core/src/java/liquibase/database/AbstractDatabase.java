package liquibase.database;

import liquibase.change.*;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.structure.*;
import liquibase.database.template.Executor;
import liquibase.database.template.JdbcOutputTemplate;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.*;
import liquibase.lock.LockManager;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.log.LogFactory;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * AbstractDatabase is extended by all supported databases as a facade to the underlying database.
 * The physical connection can be retrieved from the AbstractDatabase implementation, as well as any
 * database-specific characteristics such as the datatype for "boolean" fields.
 */
public abstract class AbstractDatabase implements Database {

    private DatabaseConnection connection;
    private String defaultSchemaName;

    static final protected Logger log = LogFactory.getLogger();

    protected String currentDateTimeFunction;

    private Executor executor = new Executor(this);
    private List<RanChangeSet> ranChangeSetList;
    private static final DataType DATE_TYPE = new DataType("DATE", false);
    private static final DataType TIME_TYPE = new DataType("TIME", false);
    private static final DataType BIGINT_TYPE = new DataType("BIGINT", true);
    private static final DataType CHAR_TYPE = new DataType("CHAR", true);
    private static final DataType VARCHAR_TYPE = new DataType("VARCHAR", true);
    private static final DataType FLOAT_TYPE = new DataType("FLOAT", true);
    private static final DataType DOUBLE_TYPE = new DataType("DOUBLE", true);
    private static final DataType INT_TYPE = new DataType("INT", true);
    private static final DataType TINYINT_TYPE = new DataType("TINYINT", true);

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private String databaseChangeLogTableName = "DatabaseChangeLog".toUpperCase();
	private String databaseChangeLogLockTableName = "DatabaseChangeLogLock".toUpperCase();;

    protected AbstractDatabase() {
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
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

    public String getDatabaseProductName(Connection conn) throws JDBCException {
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

    public int getDatabaseMajorVersion() throws JDBCException {
        try {
            return connection.getMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public int getDatabaseMinorVersion() throws JDBCException {
        try {
            return connection.getMetaData().getDatabaseMinorVersion();
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

    public String getDefaultCatalogName() throws JDBCException {
        return null;
    }

    protected String getDefaultDatabaseSchemaName() throws JDBCException {
        return getConnectionUsername();
    }

    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    public void setDefaultSchemaName(String schemaName) throws JDBCException {
        this.defaultSchemaName = schemaName;
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

    public boolean supportsAutoIncrement() {
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
    public String getColumnType(String columnType, Boolean autoIncrement) {
        // Parse out data type and precision
        // Example cases: "CLOB", "java.sql.Types.CLOB", "CLOB(10000)", "java.sql.Types.CLOB(10000)
        String dataTypeName = null;
        String precision = null;
        if (columnType.startsWith("java.sql.Types") && columnType.contains("(")) {
            precision = columnType.substring(columnType.indexOf("(") + 1, columnType.indexOf(")"));
            dataTypeName = columnType.substring(columnType.lastIndexOf(".") + 1, columnType.indexOf("("));
        } else if (columnType.startsWith("java.sql.Types")) {
            dataTypeName = columnType.substring(columnType.lastIndexOf(".") + 1);
        } else if (columnType.contains("(")) {
            precision = columnType.substring(columnType.indexOf("(") + 1, columnType.indexOf(")"));
            dataTypeName = columnType.substring(0, columnType.indexOf("("));
        } else {
            dataTypeName = columnType;
        }

        // Translate type to database-specific type, if possible
        DataType returnTypeName = null;
        if (dataTypeName.equalsIgnoreCase("BIGINT")) {
            returnTypeName = getBigIntType();
        } else if (dataTypeName.equalsIgnoreCase("BLOB")) {
            returnTypeName = getBlobType();
        } else if (dataTypeName.equalsIgnoreCase("BOOLEAN")) {
            returnTypeName = getBooleanType();
        } else if (dataTypeName.equalsIgnoreCase("CHAR")) {
            returnTypeName = getCharType();
        } else if (dataTypeName.equalsIgnoreCase("CLOB")) {
            returnTypeName = getClobType();
        } else if (dataTypeName.equalsIgnoreCase("CURRENCY")) {
            returnTypeName = getCurrencyType();
        } else if (dataTypeName.equalsIgnoreCase("DATE")) {
            returnTypeName = getDateType();
        } else if (dataTypeName.equalsIgnoreCase("DATETIME")) {
            returnTypeName = getDateTimeType();
        } else if (dataTypeName.equalsIgnoreCase("DOUBLE")) {
            returnTypeName = getDoubleType();
        } else if (dataTypeName.equalsIgnoreCase("FLOAT")) {
            returnTypeName = getFloatType();
        } else if (dataTypeName.equalsIgnoreCase("INT")) {
            returnTypeName = getIntType();
        } else if (dataTypeName.equalsIgnoreCase("INTEGER")) {
            returnTypeName = getIntType();
        } else if (dataTypeName.equalsIgnoreCase("LONGVARBINARY")) {
            returnTypeName = getBlobType();
        } else if (dataTypeName.equalsIgnoreCase("LONGVARCHAR")) {
            returnTypeName = getClobType();
        } else if (dataTypeName.equalsIgnoreCase("TEXT")) {
            returnTypeName = getClobType();
        } else if (dataTypeName.equalsIgnoreCase("TIME")) {
            returnTypeName = getTimeType();
        } else if (dataTypeName.equalsIgnoreCase("TIMESTAMP")) {
            returnTypeName = getDateTimeType();
        } else if (dataTypeName.equalsIgnoreCase("TINYINT")) {
            returnTypeName = getTinyIntType();
        } else if (dataTypeName.equalsIgnoreCase("UUID")) {
            returnTypeName = getUUIDType();
        } else if (dataTypeName.equalsIgnoreCase("VARCHAR")) {
            returnTypeName = getVarcharType();
        } else {
            if (columnType.startsWith("java.sql.Types")) {
                returnTypeName = getTypeFromMetaData(dataTypeName);
            } else {
                // Don't know what it is, just return it
                return columnType;
            }
        }

        // Return type and precision, if any
        if (precision != null && returnTypeName.getSupportsPrecision()) {
            return returnTypeName.getDataTypeName() + "(" + precision + ")";
        } else {
            return returnTypeName.getDataTypeName();
        }
    }

    // Get the type from the Connection MetaData (use the MetaData to translate from java.sql.Types to DB-specific type)
    private DataType getTypeFromMetaData(final String dataTypeName)
    {
        ResultSet resultSet = null;
        try {
            Integer requestedType = (Integer) Class.forName("java.sql.Types").getDeclaredField(dataTypeName).get(null);
            DatabaseConnection connection = getConnection();
            if (connection == null) {
                throw new RuntimeException("Cannot evaluate java.sql.Types without a connection");
            }
            resultSet = connection.getMetaData().getTypeInfo();
            while (resultSet.next()) {
                String typeName = resultSet.getString("TYPE_NAME");
                int dataType = resultSet.getInt("DATA_TYPE");
                int maxPrecision = resultSet.getInt("PRECISION");
                if (requestedType == dataType) {
                    if (maxPrecision > 0) {
                        return new DataType(typeName, true);
                    } else {
                        return new DataType(typeName, false);
                    }
                }
            }
            // Connection MetaData does not contain the type, return null
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // Can't close result set, no handling required
                }
            }
        }
    }

    public final String getColumnType(ColumnConfig columnConfig) {
        return getColumnType(columnConfig.getType(), columnConfig.isAutoIncrement());
    }

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
     * yyyy-MM-dd
     * hh:mm:ss
     * yyyy-MM-ddThh:mm:ss
     */
    public String getDateLiteral(String isoDate) {
        if (isDateOnly(isoDate) || isTimeOnly(isoDate)) {
            return "'" + isoDate + "'";
        } else if (isDateTime(isoDate)) {
//            StringBuffer val = new StringBuffer();
//            val.append("'");
//            val.append(isoDate.substring(0, 10));
//            val.append(" ");
////noinspection MagicNumber
//            val.append(isoDate.substring(11));
//            val.append("'");
//            return val.toString();
            return "'" + isoDate.replace('T', ' ') + "'";
        } else {
            return "BAD_DATE_FORMAT:" + isoDate;
        }
    }


    public String getDateLiteral(java.sql.Timestamp date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    public String getDateLiteral(java.sql.Date date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    public String getDateLiteral(java.sql.Time date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    public String getDateLiteral(Date date) {
        if (date instanceof java.sql.Date) {
            return getDateLiteral(((java.sql.Date) date));
        } else if (date instanceof java.sql.Time) {
            return getDateLiteral(((java.sql.Time) date));
        } else if (date instanceof Timestamp) {
            return getDateLiteral(((Timestamp) date));
        } else if (date instanceof ComputedDateValue) {
            return date.toString();
        } else {
            throw new RuntimeException("Unexpected type: " + date.getClass().getName());
        }
    }

    protected Date parseDate(String dateAsString) throws DateParseException {
        try {
            if (dateAsString.indexOf(" ") > 0) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateAsString);
            } else if (dateAsString.indexOf("T") > 0) {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateAsString);                
            } else {
                if (dateAsString.indexOf(":") > 0) {
                    return new SimpleDateFormat("HH:mm:ss").parse(dateAsString);
                } else {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(dateAsString);
                }
            }
        } catch (ParseException e) {
            throw new DateParseException(dateAsString);
        }
    }

    protected boolean isDateOnly(String isoDate) {
        return isoDate.length() == "yyyy-MM-dd".length();
    }

    protected boolean isDateTime(String isoDate) {
        return isoDate.length() >= "yyyy-MM-ddThh:mm:ss".length();
    }

    protected boolean isTimeOnly(String isoDate) {
        return isoDate.length() == "hh:mm:ss".length();
    }

    /**
     * Returns the actual database-specific data type to use a "date" (no time information) column.
     */
    public DataType getDateType() {
        return DATE_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use a "time" column.
     */
    public DataType getTimeType() {
        return TIME_TYPE;
    }

    public DataType getBigIntType() {
        return BIGINT_TYPE;
    }

    /** Returns the actual database-specific data type to use for a "char" column. */
    public DataType getCharType()
    {
        return CHAR_TYPE;
    }

    /** Returns the actual database-specific data type to use for a "varchar" column. */
    public DataType getVarcharType()
    {
        return VARCHAR_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "float" column.
     *
     * @return database-specific type for float
     */
    public DataType getFloatType()
    {
        return FLOAT_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "double" column.
     *
     * @return database-specific type for double
     */
    public DataType getDoubleType()
    {
        return DOUBLE_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "int" column.
     *
     * @return database-specific type for int
     */
    public DataType getIntType()
    {
        return INT_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "tinyint" column.
     *
     * @return database-specific type for tinyint
     */
    public DataType getTinyIntType()
    {
        return TINYINT_TYPE;
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

    public String getConcatSql(String... values) {
        StringBuffer returnString = new StringBuffer();
        for (String value : values) {
            returnString.append(value).append(" || ");
        }

        return returnString.toString().replaceFirst(" \\|\\| $", "");
    }

// ------- DATABASECHANGELOG / DATABASECHANGELOGLOCK METHODS ---- //

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogTableName()
     */
    public String getDatabaseChangeLogTableName() {
        return databaseChangeLogTableName;
    }

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogLockTableName()
     */
    public String getDatabaseChangeLogLockTableName() {
        return databaseChangeLogLockTableName;
    }
    
    /**
     * @see liquibase.database.Database#setDatabaseChangeLogTableName(java.lang.String)
     */
    public void setDatabaseChangeLogTableName(String tableName) {
        this.databaseChangeLogTableName = tableName;
    }

    /**
     * @see liquibase.database.Database#setDatabaseChangeLogLockTableName(java.lang.String)
     */
    public void setDatabaseChangeLogLockTableName(String tableName) {
        this.databaseChangeLogLockTableName = tableName;
    }

    private SqlStatement getChangeLogLockInsertSQL() {
        return new InsertStatement(getDefaultSchemaName(), getDatabaseChangeLogLockTableName())
                .addColumnValue("ID", 1)
                .addColumnValue("LOCKED", Boolean.FALSE);
    }

    protected SqlStatement getCreateChangeLogLockSQL() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }

    protected SqlStatement getCreateChangeLogSQL() {
        return new CreateTableStatement(getDefaultSchemaName(), getDatabaseChangeLogTableName())
                .addPrimaryKeyColumn("ID", "VARCHAR(63)", null, null, new NotNullConstraint())
                .addPrimaryKeyColumn("AUTHOR", "VARCHAR(63)", null, null, new NotNullConstraint())
                .addPrimaryKeyColumn("FILENAME", "VARCHAR(200)", null, null, new NotNullConstraint())
                .addColumn("DATEEXECUTED", getDateTimeType().getDataTypeName(), new NotNullConstraint())
                .addColumn("MD5SUM", "VARCHAR(32)")
                .addColumn("DESCRIPTION", "VARCHAR(255)")
                .addColumn("COMMENTS", "VARCHAR(255)")
                .addColumn("TAG", "VARCHAR(255)")
                .addColumn("LIQUIBASE", "VARCHAR(10)");
    }

    public SqlStatement getSelectChangeLogLockSQL() throws JDBCException {
        return new RawSqlStatement(("SELECT LOCKED FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName()) + " WHERE " + escapeColumnName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName(), "ID") + "=1"));
    }


    public boolean doesChangeLogTableExist() throws JDBCException {
        DatabaseConnection connection = getConnection();
        ResultSet rs = null;
        try {
            rs = connection.getMetaData().getTables(convertRequestedSchemaToCatalog(getDefaultSchemaName()), convertRequestedSchemaToSchema(getDefaultSchemaName()), getDatabaseChangeLogTableName(), new String[]{"TABLE"});
            return rs.next();
        } catch (Exception e) {
            throw new JDBCException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.warning("Error closing result set: " + e.getMessage());
                }
            }
        }

    }

    /**
     * This method will check the database ChangeLog table used to keep track of
     * the changes in the file. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     */
    public void checkDatabaseChangeLogTable() throws JDBCException {
        if (!this.getJdbcTemplate().executesStatements()) {
            if (((JdbcOutputTemplate) this.getJdbcTemplate()).alreadyCreatedChangeTable()) {
                return;
            } else {
                ((JdbcOutputTemplate) this.getJdbcTemplate()).setAlreadyCreatedChangeTable(true);
            }
        }

        DatabaseConnection connection = getConnection();
        ResultSet checkColumnsRS = null;
        List<SqlStatement> statementsToExecute = new ArrayList<SqlStatement>();

        boolean changeLogCreateAttempted = false;
        try {
            if (doesChangeLogTableExist()) {
                checkColumnsRS = connection.getMetaData().getColumns(convertRequestedSchemaToCatalog(getDefaultSchemaName()), convertRequestedSchemaToSchema(getDefaultSchemaName()), getDatabaseChangeLogTableName(), null);
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
                    statementsToExecute.add(new AddColumnStatement(getDefaultSchemaName(), getDatabaseChangeLogTableName(), "DESCRIPTION", "VARCHAR(255)", null));
                }
                if (!hasTag) {
                    statementsToExecute.add(new AddColumnStatement(getDefaultSchemaName(), getDatabaseChangeLogTableName(), "TAG", "VARCHAR(255)", null));
                }
                if (!hasComments) {
                    statementsToExecute.add(new AddColumnStatement(getDefaultSchemaName(), getDatabaseChangeLogTableName(), "COMMENTS", "VARCHAR(255)", null));
                }
                if (!hasLiquibase) {
                    statementsToExecute.add(new AddColumnStatement(getDefaultSchemaName(), getDatabaseChangeLogTableName(), "LIQUIBASE", "VARCHAR(255)", null));
                }

            } else if (!changeLogCreateAttempted) {
                getJdbcTemplate().comment("Create Database Change Log Table");
                SqlStatement createTableStatement = getCreateChangeLogSQL();
                if (!canCreateChangeLogTable()) {
                    throw new JDBCException("Cannot create " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()) + " table for your database.\n\n" +
                            "Please construct it manually using the following SQL as a base and re-run LiquiBase:\n\n" +
                            createTableStatement);
                }
                // If there is no table in the database for recording change history create one.
                statementsToExecute.add(createTableStatement);
                log.info("Creating database history table with name: " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()));
//                }
            }

            for (SqlStatement sql : statementsToExecute) {
                this.getJdbcTemplate().execute(sql, new ArrayList<SqlVisitor>());
                this.commit();
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
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

    public boolean doesChangeLogLockTableExist() throws JDBCException {
        DatabaseConnection connection = getConnection();
        ResultSet rs = null;
        try {
            rs = connection.getMetaData().getTables(convertRequestedSchemaToCatalog(getDefaultSchemaName()), convertRequestedSchemaToSchema(getDefaultSchemaName()), getDatabaseChangeLogLockTableName(), new String[]{"TABLE"});
            return rs.next();
        } catch (Exception e) {
            throw new JDBCException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.warning("Error closing result set: " + e.getMessage());
                }
            }
        }

    }

    /**
     * This method will check the database ChangeLogLock table used to keep track of
     * if a machine is updating the database. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     */
    public void checkDatabaseChangeLogLockTable() throws JDBCException {
        boolean knowMustInsertIntoLockTable = false;

        if (!doesChangeLogLockTableExist()) {
            if (!this.getJdbcTemplate().executesStatements()) {
                if (((JdbcOutputTemplate) this.getJdbcTemplate()).alreadyCreatedChangeLockTable()) {
                    return;
                } else {
                    ((JdbcOutputTemplate) this.getJdbcTemplate()).setAlreadyCreatedChangeLockTable(true);
                }
            }


            SqlStatement createTableStatement = getCreateChangeLogLockSQL();

            getJdbcTemplate().comment("Create Database Lock Table");
            this.getJdbcTemplate().execute(createTableStatement, new ArrayList<SqlVisitor>());
            this.commit();
            log.finest("Created database lock table with name: " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName()));
            knowMustInsertIntoLockTable = true;
        }

        int rows = -1;
        if (!knowMustInsertIntoLockTable) {
            RawSqlStatement selectStatement = new RawSqlStatement("SELECT COUNT(*) FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName()) + " WHERE ID=1");
            try {
                rows = this.getJdbcTemplate().queryForInt(selectStatement, new ArrayList<SqlVisitor>());
            } catch (JDBCException e) {
                throw e;
            }
        }
        if (knowMustInsertIntoLockTable || rows == 0) {
            this.getJdbcTemplate().update(getChangeLogLockInsertSQL(), new ArrayList<SqlVisitor>());
            this.commit();
            log.fine("Inserted lock row into: " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName()));
        }

    }

// ------- DATABASE OBJECT DROPPING METHODS ---- //

    /**
     * Drops all objects owned by the connected user.
     *
     * @param schema
     */
    public void dropDatabaseObjects(String schema) throws JDBCException {
        try {
            DatabaseSnapshot snapshot = createDatabaseSnapshot(schema, new HashSet<DiffStatusListener>());

            List<Change> dropChanges = new ArrayList<Change>();

            for (View view : snapshot.getViews()) {
                DropViewChange dropChange = new DropViewChange();
                dropChange.setViewName(view.getName());
                dropChange.setSchemaName(schema);

                dropChanges.add(dropChange);
            }

            for (ForeignKey fk : snapshot.getForeignKeys()) {
                DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
                dropFK.setBaseTableSchemaName(schema);
                dropFK.setBaseTableName(fk.getForeignKeyTable().getName());
                dropFK.setConstraintName(fk.getName());

                dropChanges.add(dropFK);
            }

//            for (Index index : snapshot.getIndexes()) {
//                DropIndexChange dropChange = new DropIndexChange();
//                dropChange.setIndexName(index.getName());
//                dropChange.setSchemaName(schema);
//                dropChange.setTableName(index.getTableName());
//
//                dropChanges.add(dropChange);
//            }

            for (Table table : snapshot.getTables()) {
                DropTableChange dropChange = new DropTableChange();
                dropChange.setSchemaName(schema);
                dropChange.setTableName(table.getName());
                dropChange.setCascadeConstraints(true);

                dropChanges.add(dropChange);
            }

            if (this.supportsSequences()) {
                for (Sequence seq : snapshot.getSequences()) {
                    DropSequenceChange dropChange = new DropSequenceChange();
                    dropChange.setSequenceName(seq.getName());
                    dropChange.setSchemaName(schema);

                    dropChanges.add(dropChange);
                }
            }


            if (snapshot.hasDatabaseChangeLogTable()) {
                RawSQLChange clearChangeLogChange = new RawSQLChange();
                clearChangeLogChange.setSql("DELETE FROM " + escapeTableName(convertRequestedSchemaToSchema(schema), getDatabaseChangeLogTableName()));
                dropChanges.add(clearChangeLogChange);
            }

            for (Change change : dropChanges) {
                for (SqlStatement statement : change.generateStatements(this)) {
                    this.getJdbcTemplate().execute(statement, new ArrayList<SqlVisitor>());
                }
            }

        } finally {
            this.commit();
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

    public boolean isSystemView(String catalogName, String schemaName, String viewName) {
        if ("information_schema".equalsIgnoreCase(schemaName)) {
            return true;
        } else if (getSystemTablesAndViews().contains(viewName)) {
            return true;
        }
        return false;
    }

    public boolean isLiquibaseTable(String tableName) {
        return tableName.equalsIgnoreCase(this.getDatabaseChangeLogTableName()) || tableName.equalsIgnoreCase(this.getDatabaseChangeLogLockTableName());
    }

// ------- DATABASE TAGGING METHODS ---- //

    /**
     * Tags the database changelog with the given string.
     */
    public void tag(String tagString) throws JDBCException {
        try {
            int totalRows = this.getJdbcTemplate().queryForInt(new RawSqlStatement("SELECT COUNT(*) FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName())), new ArrayList<SqlVisitor>());
            if (totalRows == 0) {
                throw new JDBCException("Cannot tag an empty database");
            }

//            Timestamp lastExecutedDate = (Timestamp) this.getJdbcTemplate().queryForObject(createChangeToTagSQL(), Timestamp.class);
            int rowsUpdated = this.getJdbcTemplate().update(new TagDatabaseStatement(tagString), new ArrayList<SqlVisitor>());
            if (rowsUpdated == 0) {
                throw new JDBCException("Did not tag database change log correctly");
            }
            this.commit();
        } catch (Exception e) {
            throw new JDBCException(e);
        }
    }

    public SqlStatement createFindSequencesSQL(String schema) throws JDBCException {
        return null;
    }

    public boolean doesTagExist(String tag) throws JDBCException {
        int count = this.getJdbcTemplate().queryForInt(new RawSqlStatement("SELECT COUNT(*) FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()) + " WHERE TAG='" + tag + "'"), new ArrayList<SqlVisitor>());
        return count > 0;
    }

    public String toString() {
        if (getConnection() == null) {
            return getProductName() + " Database";
        }
        try {
            return getConnectionUsername() + " @ " + getConnectionURL() + (getDefaultSchemaName() == null ? "" : " (Default Schema: " + getDefaultSchemaName() + ")");
        } catch (JDBCException e) {
            return super.toString();
        }
    }


    public boolean shouldQuoteValue(String value) {
        return true;
    }

    public String getViewDefinition(String schemaName, String viewName) throws JDBCException {
        if (schemaName == null) {
            schemaName = convertRequestedSchemaToSchema(null);
        }
        String definition = (String) this.getJdbcTemplate().queryForObject(getViewDefinitionSql(schemaName, viewName), String.class, new ArrayList<SqlVisitor>());
        if (definition == null) {
            return null;
        }
        return CREATE_VIEW_AS_PATTERN.matcher(definition).replaceFirst("");
    }

    public SqlStatement getViewDefinitionSql(String schemaName, String viewName) throws JDBCException {
        String sql = "select view_definition from information_schema.views where upper(table_name)='" + viewName.toUpperCase() + "'";
        if (convertRequestedSchemaToCatalog(schemaName) != null) {
            sql += " and table_schema='" + convertRequestedSchemaToSchema(schemaName) + "'";
        } else if (convertRequestedSchemaToCatalog(schemaName) != null) {
            sql += " and table_catalog='" + convertRequestedSchemaToCatalog(schemaName) + "'";
        }

        log.finest("GetViewDefinitionSQL: " + sql);
        return new RawSqlStatement(sql);
    }


    public int getDatabaseType(int type) {
        int returnType = type;
        if (returnType == Types.BOOLEAN) {
            String booleanType = getBooleanType().getDataTypeName();
            if (!booleanType.equalsIgnoreCase("boolean")) {
                returnType = Types.TINYINT;
            }
        }

        return returnType;
    }

    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits) throws ParseException {
        if (defaultValue == null) {
            return null;
        } else if (defaultValue instanceof String) {
            return convertToCorrectJavaType(((String) defaultValue).replaceFirst("^'", "").replaceFirst("'$", ""), dataType, columnSize, decimalDigits);
        } else {
            return defaultValue;
        }
    }

    protected Object convertToCorrectJavaType(String value, int dataType, int columnSize, int decimalDigits) throws ParseException {
        if (value == null) {
            return null;
        }
        if (dataType == Types.CLOB || dataType == Types.VARCHAR || dataType == Types.CHAR || dataType == Types.LONGVARCHAR) {
            if (value.equalsIgnoreCase("NULL")) {
                return null;
            } else {
                return value;
            }
        }

        value = StringUtils.trimToNull(value);
        if (value == null) {
            return null;
        }

        try {
            if (dataType == Types.DATE) {
                return new java.sql.Date(parseDate(value).getTime());
            } else if (dataType == Types.TIMESTAMP) {
                return new Timestamp(parseDate(value).getTime());
            } else if (dataType == Types.TIME) {
                return new Time(parseDate(value).getTime());
            } else if (dataType == Types.BIGINT) {
                return new BigInteger(value);
            } else if (dataType == Types.BIT) {
                value = value.replaceFirst("b'",""); //mysql puts wierd chars in bit field
                if (value.equalsIgnoreCase("true")) {
                    return Boolean.TRUE;
                } else if (value.equalsIgnoreCase("false")) {
                    return Boolean.FALSE;
                } else if (value.equals("1")) {
                    return Boolean.TRUE;
                } else if (value.equals("0")) {
                    return Boolean.FALSE;
                } else if (value.equals("(1)")) {
                    return Boolean.TRUE;
                } else if (value.equals("(0)")) {
                    return Boolean.FALSE;
                }
                throw new ParseException("Unknown bit value: " + value, 0);
            } else if (dataType == Types.BOOLEAN) {
                return Boolean.valueOf(value);
            } else if (dataType == Types.DECIMAL) {
                if (decimalDigits == 0) {
                    return new Integer(value);
                }
                return new Double(value);
            } else if (dataType == Types.DOUBLE || dataType == Types.NUMERIC) {
                return new Double(value);
            } else if (dataType == Types.FLOAT) {
                return new Float(value);
            } else if (dataType == Types.INTEGER) {
                return new Integer(value);
            } else if (dataType == Types.NULL) {
                return null;
            } else if (dataType == Types.REAL) {
                return new Float(value);
            } else if (dataType == Types.SMALLINT) {
                return new Integer(value);
            } else if (dataType == Types.TINYINT) {
                return new Integer(value);
            } else if (dataType == Types.BLOB) {
                return "!!!!!! LIQUIBASE CANNOT OUTPUT BLOB VALUES !!!!!!";
            } else {
                log.warning("Do not know how to convert type " + dataType);
                return value;
            }
        } catch (DateParseException e) {
            return new ComputedDateValue(value);
        } catch (NumberFormatException e) {
            return new ComputedNumericValue(value);
        }
    }

    public String convertJavaObjectToString(Object value) {
        if (value != null) {
            if (value instanceof String) {
                if ("null".equalsIgnoreCase(((String) value))) {
                    return null;
                }
                return "'" + ((String) value).replaceAll("'", "''") + "'";
            } else if (value instanceof Number) {
                return value.toString();
            } else if (value instanceof Boolean) {
                String returnValue;
                if (((Boolean) value)) {
                    returnValue = this.getTrueBooleanValue();
                } else {
                    returnValue = this.getFalseBooleanValue();
                }
                if (returnValue.matches("\\d+")) {
                    return returnValue;
                } else {
                    return "'" + returnValue + "'";
                }
            } else if (value instanceof java.sql.Date) {
                return this.getDateLiteral(((java.sql.Date) value));
            } else if (value instanceof java.sql.Time) {
                return this.getDateLiteral(((java.sql.Time) value));
            } else if (value instanceof java.sql.Timestamp) {
                return this.getDateLiteral(((java.sql.Timestamp) value));
            } else if (value instanceof ComputedDateValue) {
                return ((ComputedDateValue) value).getValue();
            } else {
                throw new RuntimeException("Unknown default value type: " + value.getClass().getName());
            }
        } else {
            return null;
        }
    }

    public String escapeTableName(String schemaName, String tableName) {
        if (schemaName == null) {
            schemaName = getDefaultSchemaName();
        }

        if (StringUtils.trimToNull(schemaName) == null || !supportsSchemas()) {
            return escapeDatabaseObject(tableName);
        } else {
            return escapeDatabaseObject(schemaName)+"."+escapeDatabaseObject(tableName);
        }
    }

    public String escapeDatabaseObject(String objectName) {
        return objectName;
    }

    public String escapeIndexName(String schemaName, String indexName) {
        if (StringUtils.trimToNull(schemaName) == null || !supportsSchemas()) {
            return escapeDatabaseObject(indexName);
        } else {
            return escapeDatabaseObject(schemaName)+"."+escapeDatabaseObject(indexName);
        }
    }

    public String escapeSequenceName(String schemaName, String sequenceName) {
        if (schemaName == null) {
            schemaName = getDefaultSchemaName();
        }

        if (StringUtils.trimToNull(schemaName) == null || !supportsSchemas()) {
            return escapeDatabaseObject(sequenceName);
        } else {
            return escapeDatabaseObject(schemaName)+"."+escapeDatabaseObject(sequenceName);
        }
    }

    public String escapeConstraintName(String constraintName) {
        return escapeDatabaseObject(constraintName);
    }

    public String escapeColumnName(String schemaName, String tableName, String columnName) {
        if (schemaName == null) {
            schemaName = getDefaultSchemaName();
        }

        return escapeDatabaseObject(columnName);
    }

    public String escapeColumnNameList(String columnNames) {
        StringBuffer sb = new StringBuffer();
        for(String columnName : columnNames.split(",")) {
            if(sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(escapeDatabaseObject(columnName.trim()));
        }
        return sb.toString();

    }

    public String convertRequestedSchemaToCatalog(String requestedSchema) throws JDBCException {
        if (getDefaultCatalogName() == null) {
            return null;
        } else {
            if (requestedSchema == null) {
                return getDefaultCatalogName();
            }
            return StringUtils.trimToNull(requestedSchema);
        }
    }

    public String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException {
        String returnSchema = requestedSchema;
        if (returnSchema == null) {
            returnSchema = getDefaultDatabaseSchemaName();
        }

        if (returnSchema != null) {
            returnSchema = returnSchema.toUpperCase();
        }
        return returnSchema;
    }

    public boolean supportsSchemas() {
        return true;
    }

    public String generatePrimaryKeyName(String tableName) {
        return "PK_" + tableName.toUpperCase();
    }

    public String escapeViewName(String schemaName, String viewName) {
        return escapeTableName(schemaName, viewName);
    }

    public boolean isColumnAutoIncrement(String schemaName, String tableName, String columnName) throws SQLException, JDBCException {
        if (!supportsAutoIncrement()) {
            return false;
        }

        boolean autoIncrement = false;

        ResultSet selectRS = null;
        try {
            selectRS = getConnection().createStatement().executeQuery("SELECT " + escapeColumnName(schemaName, tableName, columnName) + " FROM " + escapeTableName(schemaName, tableName) + " WHERE 1 = 0");
            ResultSetMetaData meta = selectRS.getMetaData();
            autoIncrement = meta.isAutoIncrement(1);
        } finally {
            if (selectRS != null) {
                selectRS.close();
            }
        }

        return autoIncrement;
    }

    /**
     * Returns the run status for the given ChangeSet
     */
    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException {
        if (!doesChangeLogTableExist()) {
            return ChangeSet.RunStatus.NOT_RAN;
        }

        RanChangeSet foundRan = getRanChangeSet(changeSet);

        if (foundRan == null) {
            return ChangeSet.RunStatus.NOT_RAN;
        } else {
            if (foundRan.getMd5sum() == null) {
                try {
                    log.info("Updating NULL md5sum for " + changeSet.toString());
                    DatabaseConnection connection = getConnection();
                    PreparedStatement updatePstmt = connection.prepareStatement("UPDATE " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM=? WHERE ID=? AND AUTHOR=? AND FILENAME=?");
                    updatePstmt.setString(1, changeSet.getMd5sum());
                    updatePstmt.setString(2, changeSet.getId());
                    updatePstmt.setString(3, changeSet.getAuthor());
                    updatePstmt.setString(4, changeSet.getFilePath());

                    updatePstmt.executeUpdate();
                    updatePstmt.close();
                    this.commit();
                } catch (SQLException e) {
                    throw new JDBCException(e);
                }

                return ChangeSet.RunStatus.ALREADY_RAN;
            } else {
                if (foundRan.getMd5sum().equals(changeSet.getMd5sum())) {
                    return ChangeSet.RunStatus.ALREADY_RAN;
                } else {
                    if (changeSet.shouldRunOnChange()) {
                        return ChangeSet.RunStatus.RUN_AGAIN;
                    } else {
                        return ChangeSet.RunStatus.INVALID_MD5SUM;
//                        throw new DatabaseHistoryException("MD5 Check for " + changeSet.toString() + " failed");
                    }
                }
            }
        }
    }

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException {
        if (!doesChangeLogTableExist()) {
            throw new DatabaseHistoryException("Database change table does not exist");
        }

        RanChangeSet foundRan = null;
        for (RanChangeSet ranChange : getRanChangeSetList()) {
            if (ranChange.isSameAs(changeSet)) {
                foundRan = ranChange;
                break;
            }
        }
        return foundRan;
    }

    /**
     * Returns the ChangeSets that have been run against the current database.
     */
    public List<RanChangeSet> getRanChangeSetList() throws JDBCException {
        if (this.ranChangeSetList != null) {
            return this.ranChangeSetList;
        }

        try {
            String databaseChangeLogTableName = escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName());
            ranChangeSetList = new ArrayList<RanChangeSet>();
            if (doesChangeLogTableExist()) {
                log.info("Reading from " + databaseChangeLogTableName);
                String sql = "SELECT * FROM " + databaseChangeLogTableName + " ORDER BY DATEEXECUTED ASC".toUpperCase();
                Statement statement = getConnection().createStatement();
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    String fileName = rs.getString("FILENAME");
                    String author = rs.getString("AUTHOR");
                    String id = rs.getString("ID");
                    String md5sum = rs.getString("MD5SUM");
                    Date dateExecuted = rs.getTimestamp("DATEEXECUTED");
                    String tag = rs.getString("TAG");
                    RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, md5sum, dateExecuted, tag);
                    ranChangeSetList.add(ranChangeSet);
                }
                rs.close();
                statement.close();
            }
            return ranChangeSetList;
        } catch (SQLException e) {
            if (!getJdbcTemplate().executesStatements()) {
                //probably not created, no problem
                return new ArrayList<RanChangeSet>();
            } else {
                throw new JDBCException(e);
            }
        }
    }

    public Date getRanDate(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException {
        RanChangeSet ranChange = getRanChangeSet(changeSet);
        if (ranChange == null) {
            return null;
        } else {
            return ranChange.getDateExecuted();
        }
    }

    /**
     * After the change set has been ran against the database this method will update the change log table
     * with the information.
     */
    public void markChangeSetAsRan(ChangeSet changeSet) throws JDBCException {
        String dateValue = getCurrentDateTimeFunction();

        InsertStatement statement = new InsertStatement(getDefaultSchemaName(), getDatabaseChangeLogTableName());
        statement.addColumnValue("ID", escapeStringForDatabase(changeSet.getId()));
        statement.addColumnValue("AUTHOR", changeSet.getAuthor());
        statement.addColumnValue("FILENAME", changeSet.getFilePath());
        statement.addColumnValue("DATEEXECUTED", new ComputedDateValue(dateValue));
        statement.addColumnValue("MD5SUM", changeSet.getMd5sum());
        statement.addColumnValue("DESCRIPTION", limitSize(changeSet.getDescription()));
        statement.addColumnValue("COMMENTS", limitSize(StringUtils.trimToEmpty(changeSet.getComments())));
        statement.addColumnValue("LIQUIBASE", LiquibaseUtil.getBuildVersion());


        this.getJdbcTemplate().execute(statement, new ArrayList<SqlVisitor>());

        getRanChangeSetList().add(new RanChangeSet(changeSet));
    }

    public void markChangeSetAsReRan(ChangeSet changeSet) throws JDBCException {
        String dateValue = getCurrentDateTimeFunction();
        String sql = "UPDATE " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()) + " SET DATEEXECUTED=" + dateValue + ", MD5SUM='?' WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getMd5sum()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getId()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getAuthor()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getFilePath()));

        this.getJdbcTemplate().execute(new RawSqlStatement(sql), new ArrayList<SqlVisitor>());
        this.commit();
    }

    public void removeRanStatus(ChangeSet changeSet) throws JDBCException {
        String sql = "DELETE FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()) + " WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getId()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getAuthor()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getFilePath()));

        this.getJdbcTemplate().execute(new RawSqlStatement(sql), new ArrayList<SqlVisitor>());
        commit();

        getRanChangeSetList().remove(new RanChangeSet(changeSet));
    }

    public String escapeStringForDatabase(String string) {
        return string.replaceAll("'", "''");
    }

    private String limitSize(String string) {
        int maxLength = 255;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }

    public void commit() throws JDBCException {
        try {
            getConnection().commit();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public void rollback() throws JDBCException {
        try {
            getConnection().rollback();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public Executor getJdbcTemplate() {
        return executor;
    }

    public void setJdbcTemplate(Executor template) {
        if (this.executor != null && !this.executor.executesStatements() && template.executesStatements()) {
            //need to clear any history
            LockManager.getInstance(this).reset();
        }
        this.executor = template;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractDatabase that = (AbstractDatabase) o;

        return !(connection != null ? !connection.equals(that.connection) : that.connection != null);

    }

    public int hashCode() {
        return (connection != null ? connection.hashCode() : 0);
    }

    public void close() throws JDBCException {
        try {
            DatabaseConnection connection = getConnection();
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public abstract DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException;

    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    public boolean isAutoCommit() throws JDBCException {
        try {
            return getConnection().getAutoCommit();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    public void setAutoCommit(boolean b) throws JDBCException {
        try {
            getConnection().setAutoCommit(b);
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }
    
    /**
     * Default implementation, just look for "local" IPs
     * @throws JDBCException 
     */
    public boolean isLocalDatabase() throws JDBCException {
    	String url = getConnectionURL();
    	return (url.indexOf("localhost") >= 0) || (url.indexOf("127.0.0.1") >= 0);
    }

    public void executeStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException {
        SqlStatement[] statements = change.generateStatements(this);

        execute(statements, sqlVisitors);
    }

    /*
     * Executes the statements passed as argument to a target {@link Database}
     *
     * @param statements an array containing the SQL statements to be issued
     * @param database the target {@link Database}
     * @throws JDBCException if there were problems issuing the statements
     */
    public void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        for (SqlStatement statement : statements) {
            LogFactory.getLogger().finest("Executing Statement: " + statement);
            getJdbcTemplate().execute(statement, sqlVisitors);
        }
    }
    

    public void saveStatements(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        SqlStatement[] statements = change.generateStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(sql.getEndDelimiter()).append(StreamUtil.getLineSeparator()).append(StreamUtil.getLineSeparator());
            }
        }
    }

    public void executeRollbackStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException, RollbackImpossibleException {
        SqlStatement[] statements = change.generateRollbackStatements(this);
        execute(statements, sqlVisitors);
    }

    public void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        SqlStatement[] statements = change.generateRollbackStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(sql.getEndDelimiter()).append("\n\n");
            }
        }
    }
}
