package liquibase.database;

import liquibase.ChangeSet;
import liquibase.RanChangeSet;
import liquibase.change.*;
import liquibase.database.sql.*;
import liquibase.database.structure.*;
import liquibase.database.template.JdbcTemplate;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.DateParseException;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.log.LogFactory;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private String defaultSchemaName;

    static final protected Logger log = LogFactory.getLogger();
    protected boolean changeLogTableExists;
    protected boolean changeLogLockTableExists;
    protected boolean changeLogCreateAttempted;
    protected boolean changeLogLockCreateAttempted;

    protected String currentDateTimeFunction;

    private JdbcTemplate jdbcTemplate = new JdbcTemplate(this);

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
        if (columnType.startsWith("java.sql.Types")) {
            String dataTypeName = columnType.substring(columnType.lastIndexOf(".") + 1);
            String precision = null;
            if (dataTypeName.indexOf("(") >= 0) {
                precision = dataTypeName.substring(dataTypeName.indexOf("(") + 1, dataTypeName.indexOf(")"));
                dataTypeName = dataTypeName.substring(0, dataTypeName.indexOf("("));
            }

            ResultSet resultSet = null;
            try {
                DatabaseConnection connection = getConnection();
                if (connection == null) {
                    throw new RuntimeException("Cannot evaluate java.sql.Types without a connection");
                }
                resultSet = connection.getMetaData().getTypeInfo();
                while (resultSet.next()) {
                    String typeName = resultSet.getString("TYPE_NAME");
                    int dataType = resultSet.getInt("DATA_TYPE");
                    Integer requestedType = (Integer) Class.forName("java.sql.Types").getDeclaredField(dataTypeName).get(null);
                    if (requestedType == dataType) {
                        if (precision == null) {
                            return typeName;
                        } else {
                            return typeName + "(" + precision + ")";
                        }
                    }
                }
                //did not find type, fall back on our defaults for ones we can figure out
                if (dataTypeName.equalsIgnoreCase("BLOB")) {
                    return getBlobType();
                } else if (dataTypeName.equalsIgnoreCase("CLOB")) {
                    return getClobType();
                } else if (dataTypeName.equalsIgnoreCase("BOOLEAN")) {
                    return getBooleanType();
                } else if (dataTypeName.equalsIgnoreCase("DATE")) {
                    return getDateType();
                } else if (dataTypeName.equalsIgnoreCase("TIME")) {
                    return getTimeType();
                }

                throw new RuntimeException("Could not find java.sql.Types value for " + dataTypeName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        ;
                    }
                }
            }
        } else if ("boolean".equalsIgnoreCase(columnType)) {
            return getBooleanType();
        } else if ("currency".equalsIgnoreCase(columnType)) {
            return getCurrencyType();
        } else if ("UUID".equalsIgnoreCase(columnType)) {
            return getUUIDType();
        } else if ("BLOB".equalsIgnoreCase(columnType)
                || "LONGVARBINARY".equalsIgnoreCase(columnType)) {
            return getBlobType();
        } else if ("CLOB".equalsIgnoreCase(columnType)
                || "TEXT".equalsIgnoreCase(columnType)
                || "LONGVARCHAR".equalsIgnoreCase(columnType)) {
            return getClobType();
        } else if ("date".equalsIgnoreCase(columnType)) {
            return getDateType();
        } else if ("time".equalsIgnoreCase(columnType)) {
            return getTimeType();
        } else if ("dateTime".equalsIgnoreCase(columnType)) {
            return getDateTimeType();
        } else if (columnType.toUpperCase().startsWith("FLOAT(")) {
            return "FLOAT";
        } else if (columnType.toUpperCase().startsWith("DOUBLE(")) {
            return "DOUBLE";
        } else {
            return columnType;
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
    public String getDateType() {
        return "DATE";
    }

    /**
     * Returns the actual database-specific data type to use a "time" column.
     */
    public String getTimeType() {
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
        return new InsertStatement(getDefaultSchemaName(), getDatabaseChangeLogLockTableName())
                .addColumnValue("ID", 1)
                .addColumnValue("LOCKED", Boolean.FALSE);
    }

    protected SqlStatement getCreateChangeLogLockSQL() {
        return new CreateTableStatement(getDefaultSchemaName(), getDatabaseChangeLogLockTableName())
                .addPrimaryKeyColumn("ID", "INT", new NotNullConstraint())
                .addColumn("LOCKED", getBooleanType(), new NotNullConstraint())
                .addColumn("LOCKGRANTED", getDateTimeType())
                .addColumn("LOCKEDBY", "VARCHAR(255)");
    }

    protected SqlStatement getCreateChangeLogSQL() {
        return new CreateTableStatement(getDefaultSchemaName(), getDatabaseChangeLogTableName())
                .addPrimaryKeyColumn("ID", "VARCHAR(63)", new NotNullConstraint())
                .addPrimaryKeyColumn("AUTHOR", "VARCHAR(63)", new NotNullConstraint())
                .addPrimaryKeyColumn("FILENAME", "VARCHAR(200)", new NotNullConstraint())
                .addColumn("DATEEXECUTED", getDateTimeType(), new NotNullConstraint())
                .addColumn("MD5SUM", "VARCHAR(32)")
                .addColumn("DESCRIPTION", "VARCHAR(255)")
                .addColumn("COMMENTS", "VARCHAR(255)")
                .addColumn("TAG", "VARCHAR(255)")
                .addColumn("LIQUIBASE", "VARCHAR(10)");
    }

    public SqlStatement getSelectChangeLogLockSQL() throws JDBCException {
        return new RawSqlStatement(("SELECT LOCKED FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName()) + " WHERE ID=1"));
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
    public void checkDatabaseChangeLogTable() throws JDBCException {
        DatabaseConnection connection = getConnection();
        ResultSet checkTableRS = null;
        ResultSet checkColumnsRS = null;
        List<SqlStatement> statementsToExecute = new ArrayList<SqlStatement>();

        try {
            checkTableRS = connection.getMetaData().getTables(convertRequestedSchemaToCatalog(getDefaultSchemaName()), convertRequestedSchemaToSchema(getDefaultSchemaName()), getDatabaseChangeLogTableName(), new String[]{"TABLE"});
            if (checkTableRS.next()) {
                changeLogTableExists = true;
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
                changeLogCreateAttempted = true;
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
                changeLogTableExists = true;
//                }
            }

            for (SqlStatement sql : statementsToExecute) {
                this.getJdbcTemplate().execute(sql);
                this.commit();
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
    public void checkDatabaseChangeLogLockTable() throws JDBCException {
        DatabaseConnection connection = getConnection();
        ResultSet rs = null;
        boolean knowMustInsertIntoLockTable = false;
        try {
            rs = connection.getMetaData().getTables(convertRequestedSchemaToCatalog(getDefaultSchemaName()), convertRequestedSchemaToSchema(getDefaultSchemaName()), getDatabaseChangeLogLockTableName(), new String[]{"TABLE"});
            if (!rs.next()) {
                if (!changeLogLockCreateAttempted) {
                    changeLogLockCreateAttempted = true;
                    SqlStatement createTableStatement = getCreateChangeLogLockSQL();

                    getJdbcTemplate().comment("Create Database Lock Table");
                    this.getJdbcTemplate().execute(createTableStatement);
                    this.commit();
                    log.info("Created database lock table with name: " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName()));
                    changeLogLockTableExists = true;
                    knowMustInsertIntoLockTable = true;
                }
            } else {
                changeLogLockTableExists = true;
            }
            rs.close();

            if (changeLogLockTableExists) {
                int rows = -1;
                if (!knowMustInsertIntoLockTable) {
                    RawSqlStatement selectStatement = new RawSqlStatement("SELECT COUNT(*) FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName()) + " WHERE ID=1");
                    rows = this.getJdbcTemplate().queryForInt(selectStatement);
                }
                if (knowMustInsertIntoLockTable || rows == 0) {
                    this.getJdbcTemplate().update(getChangeLogLockInsertSQL());
                    this.commit();
                    log.info("Inserted lock row into: " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogLockTableName()));
                    rs.close();
                }
            } else {
                throw new JDBCException("Change log lock table does not exist");
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
     *
     * @param schema
     */
    public void dropDatabaseObjects(String schema) throws JDBCException {
        try {
            DatabaseSnapshot snapshot = createDatabaseSnapshot(schema, new HashSet<DiffStatusListener>());

            List<Change> dropChanges = new ArrayList<Change>();

            for (ForeignKey fk : snapshot.getForeignKeys()) {
                DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
                dropFK.setBaseTableSchemaName(schema);
                dropFK.setBaseTableName(fk.getForeignKeyTable().getName());
                dropFK.setConstraintName(fk.getName());

                dropChanges.add(dropFK);
            }

            for (View view : snapshot.getViews()) {
                DropViewChange dropChange = new DropViewChange();
                dropChange.setViewName(view.getName());
                dropChange.setSchemaName(schema);

                dropChanges.add(dropChange);
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


            if (this.changeLogTableExists) {
                RawSQLChange clearChangeLogChange = new RawSQLChange();
                clearChangeLogChange.setSql("DELETE FROM " + escapeTableName(convertRequestedSchemaToSchema(getDefaultSchemaName()), getDatabaseChangeLogTableName()));
                dropChanges.add(clearChangeLogChange);
            }

            try {
                for (Change change : dropChanges) {
                    for (SqlStatement statement : change.generateStatements(this)) {
                        this.getJdbcTemplate().execute(statement);
                    }
                }
            } catch (UnsupportedChangeException e) {
                throw new JDBCException(e);
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
            int totalRows = this.getJdbcTemplate().queryForInt(new RawSqlStatement("SELECT COUNT(*) FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName())));
            if (totalRows == 0) {
                throw new JDBCException("Cannot tag an empty database");
            }

            Timestamp lastExecutedDate = (Timestamp) this.getJdbcTemplate().queryForObject(createChangeToTagSQL(), Timestamp.class);
            int rowsUpdated = this.getJdbcTemplate().update(createTagSQL(tagString, lastExecutedDate));
            if (rowsUpdated == 0) {
                throw new JDBCException("Did not tag database change log correctly.  Should have tagged changeset from "+lastExecutedDate.toString());
            }
            this.commit();
        } catch (Exception e) {
            throw new JDBCException(e);
        }
    }

    /**
     * Returns SQL to return the date of the most recient changeset execution.
     */
    protected SqlStatement createChangeToTagSQL() {
        return new RawSqlStatement("SELECT MAX(DATEEXECUTED) FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()));
    }

    /**
     * Returns SQL to tag the database.  SQL Contains two ?:
     * <ol>
     * <li>tag string</li>
     * <li>date executed</li>
     * </ol>
     */
    protected SqlStatement createTagSQL(String tagName, Date dateExecuted) {
        UpdateStatement statement = new UpdateStatement(getDefaultSchemaName(), getDatabaseChangeLogTableName());
        statement.addNewColumnValue("TAG", tagName);
        statement.setWhereClause("DATEEXECUTED = ?");
        statement.addWhereParameter(dateExecuted);

        return statement;
    }

    public SqlStatement createFindSequencesSQL(String schema) throws JDBCException {
        return null;
    }

    public boolean doesTagExist(String tag) throws JDBCException {
        int count = this.getJdbcTemplate().queryForInt(new RawSqlStatement("SELECT COUNT(*) FROM " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()) + " WHERE TAG='" + tag + "'"));
        return count > 0;
    }

    public String toString() {
        if (getConnection() == null) {
            return getProductName() + " Database";
        }
        try {
            return getConnectionUsername() + " @ " + getConnectionURL() + (getDefaultSchemaName() == null?"":" (Default Schema: "+getDefaultSchemaName()+")");
        } catch (JDBCException e) {
            return super.toString();
        }
    }


    public boolean shouldQuoteValue(String value) {
        return true;
    }

    public String getViewDefinition(String schemaName, String viewName) throws JDBCException {
        if (schemaName == null) {
            schemaName = convertRequestedSchemaToSchema(schemaName);
        }
        String definition = (String) this.getJdbcTemplate().queryForObject(getViewDefinitionSql(schemaName, viewName), String.class);
        if (definition == null) {
            return null;
        }
        return definition.replaceFirst("^CREATE VIEW [\\S]+ AS", "");
    }

    public SqlStatement getViewDefinitionSql(String schemaName, String viewName) throws JDBCException {
        String sql = "select view_definition from information_schema.views where upper(table_name)='" + viewName.toUpperCase() + "'";
        if (convertRequestedSchemaToCatalog(schemaName) != null) {
            sql += " and table_schema='" + convertRequestedSchemaToSchema(schemaName) + "'";
        } else if (convertRequestedSchemaToCatalog(schemaName) != null) {
            sql += " and table_catalog='" + convertRequestedSchemaToCatalog(schemaName) + "'";
        }

        log.info("GetViewDefinitionSQL: " + sql);
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
            } else {
                throw new RuntimeException("Cannot convert type: " + dataType);
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
            } else {
                throw new RuntimeException("Unknown default value type: " + value.getClass().getName());
            }
        } else {
            return null;
        }
    }

    public String escapeTableName(String schemaName, String tableName) {
        if (StringUtils.trimToNull(schemaName) == null || !supportsSchemas()) {
            return tableName;
        } else {
            return schemaName + "." + tableName;
        }
    }

    public String escapeSequenceName(String schemaName, String sequenceName) {
        if (StringUtils.trimToNull(schemaName) == null || !supportsSchemas()) {
            return sequenceName;
        } else {
            return schemaName + "." + sequenceName;
        }
    }

    public String escapeColumnName(String columnName) {
        return columnName;
    }

    public String escapeColumnNameList(String columnNames) {
        return columnNames;
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
            selectRS = getConnection().createStatement().executeQuery("SELECT " + escapeColumnName(columnName) + " FROM " + escapeTableName(schemaName, tableName) + " WHERE 1 = 0");
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
                    PreparedStatement updatePstmt = connection.prepareStatement("UPDATE "+escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName())+" SET MD5SUM=? WHERE ID=? AND AUTHOR=? AND FILENAME=?");
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
        try {
            String databaseChangeLogTableName = escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName());
            List<RanChangeSet> ranChangeSetList = new ArrayList<RanChangeSet>();
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


        this.getJdbcTemplate().execute(statement);

        getRanChangeSetList().add(new RanChangeSet(changeSet));
    }

    public void markChangeSetAsReRan(ChangeSet changeSet) throws JDBCException {
        String dateValue = getCurrentDateTimeFunction();
        String sql = "UPDATE DATABASECHANGELOG SET DATEEXECUTED=" + dateValue + ", MD5SUM='?' WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getMd5sum()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getId()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getAuthor()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getFilePath()));

        this.getJdbcTemplate().execute(new RawSqlStatement(sql));
        this.commit();
    }

    public void removeRanStatus(ChangeSet changeSet) throws JDBCException {
        String sql = "DELETE FROM "+escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName())+" WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getId()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getAuthor()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getFilePath()));

        this.getJdbcTemplate().execute(new RawSqlStatement(sql));
        commit();
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

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate template) {
        this.jdbcTemplate = template;
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

    public DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new SqlDatabaseSnapshot(this, statusListeners, schema);
    }
}
