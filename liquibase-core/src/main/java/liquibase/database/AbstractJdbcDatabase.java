package liquibase.database;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.*;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.statement.core.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 * AbstractJdbcDatabase is extended by all supported databases as a facade to the underlying database.
 * The physical connection can be retrieved from the AbstractJdbcDatabase implementation, as well as any
 * database-specific characteristics such as the datatype for "boolean" fields.
 */
public abstract class AbstractJdbcDatabase implements Database {

    private DatabaseConnection connection;
    private String defaultCatalogName;
    private String defaultSchemaName;

    protected String currentDateTimeFunction;

    /**
     * The sequence name will be substituted into the string e.g. NEXTVAL('%s')
     */
    protected String sequenceNextValueFunction;
    protected String sequenceCurrentValueFunction;
    protected String quotingStartCharacter = "\"";
    protected String quotingEndCharacter = "\"";

    // List of Database native functions.
    protected List<DatabaseFunction> dateFunctions = new ArrayList<DatabaseFunction>();

    protected List<String> unmodifiableDataTypes = new ArrayList<String>();

    private List<RanChangeSet> ranChangeSetList;

    protected void resetRanChangeSetList() {
        ranChangeSetList = null;
    }

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private String databaseChangeLogTableName = System.getProperty("liquibase.databaseChangeLogTableName") == null ? "DatabaseChangeLog".toUpperCase() : System.getProperty("liquibase.databaseChangeLogTableName");
    private String databaseChangeLogLockTableName = System.getProperty("liquibase.databaseChangeLogLockTableName") == null ? "DatabaseChangeLogLock".toUpperCase() : System.getProperty("liquibase.databaseChangeLogLockTableName");
    private String liquibaseTablespaceName = System.getProperty("liquibase.tablespaceName");
    private String liquibaseSchemaName = System.getProperty("liquibase.schemaName");
    private String liquibaseCatalogName = System.getProperty("liquibase.catalogName");

    private Integer lastChangeSetSequenceValue;
    private Boolean previousAutoCommit;

    private boolean canCacheLiquibaseTableInfo = false;
    private boolean hasDatabaseChangeLogTable = false;
    private boolean hasDatabaseChangeLogLockTable = false;

    protected BigInteger defaultAutoIncrementStartWith = BigInteger.ONE;
    protected BigInteger defaultAutoIncrementBy = BigInteger.ONE;
    // most databases either lowercase or uppercase unuqoted objects such as table and column names.
    protected Boolean unquotedObjectsAreUppercased = null;
    // whether object names should be quoted
    protected ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.LEGACY;

    public String getName() {
        return toString();
    }

    public boolean requiresPassword() {
        return true;
    }

    public boolean requiresUsername() {
        return true;
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    // ------- DATABASE INFORMATION METHODS ---- //

    public DatabaseConnection getConnection() {
        return connection;
    }

    public void setConnection(DatabaseConnection conn) {
        LogFactory.getLogger().debug("Connected to " + conn.getConnectionUserName() + "@" + conn.getURL());
        this.connection = conn;
        try {
            boolean autoCommit = conn.getAutoCommit();
            if (autoCommit == getAutoCommitMode()) {
                // Don't adjust the auto-commit mode if it's already what the database wants it to be.
                LogFactory.getLogger().debug("Not adjusting the auto commit mode; it is already " + autoCommit);
            } else {
                // Store the previous auto-commit mode, because the connection needs to be restored to it when this
                // AbstractDatabase type is closed. This is important for systems which use connection pools.
                previousAutoCommit = autoCommit;

                LogFactory.getLogger().debug("Setting auto commit to " + getAutoCommitMode() + " from " + autoCommit);
                connection.setAutoCommit(getAutoCommitMode());
            }
        } catch (DatabaseException e) {
            LogFactory.getLogger().warning("Cannot set auto commit to " + getAutoCommitMode() + " on connection");
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
        if (connection == null) {
            return getDefaultDatabaseProductName();
        }

        try {
            return connection.getDatabaseProductName();
        } catch (DatabaseException e) {
            throw new RuntimeException("Cannot get database name");
        }
    }

    protected abstract String getDefaultDatabaseProductName();


    public String getDatabaseProductVersion() throws DatabaseException {
        if (connection == null) {
            return null;
        }

        try {
            return connection.getDatabaseProductVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    public int getDatabaseMajorVersion() throws DatabaseException {
        if (connection == null) {
            return -1;
        }
        try {
            return connection.getDatabaseMajorVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    public int getDatabaseMinorVersion() throws DatabaseException {
        if (connection == null) {
            return -1;
        }
        try {
            return connection.getDatabaseMinorVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    public String getDefaultCatalogName() {
        if (defaultCatalogName == null) {
            if (connection != null) {
                try {
                    defaultCatalogName = doGetDefaultCatalogName();
                } catch (DatabaseException e) {
                    LogFactory.getLogger().info("Error getting default catalog", e);
                }
            }
        }
        return defaultCatalogName;
    }

    protected String doGetDefaultCatalogName() throws DatabaseException {
        return connection.getCatalog();
    }

    public CatalogAndSchema correctSchema(String catalog, String schema) {
        return correctSchema(new CatalogAndSchema(catalog, schema));
    }

    public CatalogAndSchema correctSchema(CatalogAndSchema schema) {
        if (schema == null) {
            return new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName());
        }
        String catalogName = StringUtils.trimToNull(schema.getCatalogName());
        String schemaName = StringUtils.trimToNull(schema.getSchemaName());

        if (supportsCatalogs() && supportsSchemas()) {
            if (catalogName == null) {
                catalogName = getDefaultCatalogName();
            } else {
                catalogName = correctObjectName(catalogName, Catalog.class);
            }

            if (schemaName == null) {
                schemaName = getDefaultSchemaName();
            } else {
                schemaName = correctObjectName(schemaName, Schema.class);
            }
        } else if (!supportsCatalogs() && !supportsSchemas()) {
            return new CatalogAndSchema(null, null);
        } else if (supportsCatalogs()) { //schema is null
            if (catalogName == null) {
                if (schemaName == null) {
                    catalogName = getDefaultCatalogName();
                } else {
                    catalogName = schemaName;
                }
            }
            schemaName = catalogName;
        } else if (supportsSchemas()) {
            if (schemaName == null) {
                if (catalogName == null) {
                    schemaName = getDefaultSchemaName();
                } else {
                    schemaName = catalogName;
                }
            }
            catalogName = schemaName;
        }
        return new CatalogAndSchema(catalogName, schemaName);

    }

    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS || unquotedObjectsAreUppercased == null
                || objectName == null || (objectName.startsWith(quotingStartCharacter) && objectName.endsWith(
                quotingEndCharacter))) {
            return objectName;
        } else if (unquotedObjectsAreUppercased == Boolean.TRUE) {
            return objectName.toUpperCase();
        } else {
            return objectName.toLowerCase();
        }
    }

    public CatalogAndSchema getDefaultSchema() {
        return new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName());

    }

    public String getDefaultSchemaName() {

        if (!supportsSchemas()) {
            return getDefaultCatalogName();
        }

        if (defaultSchemaName == null && connection != null) {
            defaultSchemaName = doGetDefaultSchemaName();
        }


        return defaultSchemaName;
    }

    /**
     * Overwrite this method to get the default schema name for the connection.
     *
     * @return
     */
    protected String doGetDefaultSchemaName() {
        try {
            ResultSet resultSet = ((JdbcConnection) connection).prepareCall("call current_schema").executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        } catch (Exception e) {
            LogFactory.getLogger().info("Error getting default schema", e);
        }
        return null;
    }

    public void setDefaultCatalogName(String defaultCatalogName) {
        this.defaultCatalogName = correctObjectName(defaultCatalogName, Catalog.class);
    }

    public void setDefaultSchemaName(String schemaName) {
        this.defaultSchemaName = correctObjectName(schemaName, Schema.class);
    }

    /**
     * Returns system (undroppable) views.
     */
    protected Set<String> getSystemTables() {
        return new HashSet<String>();
    }


    /**
     * Returns system (undroppable) views.
     */
    protected Set<String> getSystemViews() {
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
            this.dateFunctions.add(new DatabaseFunction(function));
        }
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


    public String getDateTimeLiteral(java.sql.Timestamp date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    public String getDateLiteral(java.sql.Date date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    public String getTimeLiteral(java.sql.Time date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    public String getDateLiteral(Date date) {
        if (date instanceof java.sql.Date) {
            return getDateLiteral(((java.sql.Date) date));
        } else if (date instanceof java.sql.Time) {
            return getTimeLiteral(((java.sql.Time) date));
        } else if (date instanceof java.sql.Timestamp) {
            return getDateTimeLiteral(((java.sql.Timestamp) date));
        } else {
            throw new RuntimeException("Unexpected type: " + date.getClass().getName());
        }
    }

    public Date parseDate(String dateAsString) throws DateParseException {
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
     * Returns database-specific line comment string.
     */
    public String getLineComment() {
        return "--";
    }

    /**
     * Returns database-specific auto-increment DDL clause.
     */
    public String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy) {
        if (!supportsAutoIncrement()) {
            return "";
        }

        // generate an SQL:2003 standard compliant auto increment clause by default

        String autoIncrementClause = getAutoIncrementClause();

        boolean generateStartWith = generateAutoIncrementStartWith(startWith);
        boolean generateIncrementBy = generateAutoIncrementBy(incrementBy);

        if (generateStartWith || generateIncrementBy) {
            autoIncrementClause += getAutoIncrementOpening();

            if (generateStartWith) {
                autoIncrementClause += String.format(getAutoIncrementStartWithClause(), startWith);
            }

            if (generateIncrementBy) {
                if (generateStartWith) {
                    autoIncrementClause += ", ";
                }

                autoIncrementClause += String.format(getAutoIncrementByClause(), incrementBy);
            }

            autoIncrementClause += getAutoIncrementClosing();
        }

        return autoIncrementClause;
    }

    protected String getAutoIncrementClause() {
        return "GENERATED BY DEFAULT AS IDENTITY";
    }

    protected boolean generateAutoIncrementStartWith(BigInteger startWith) {
        return startWith != null
                && !startWith.equals(defaultAutoIncrementStartWith);
    }

    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
        return incrementBy != null
                && !incrementBy.equals(defaultAutoIncrementBy);
    }

    protected String getAutoIncrementOpening() {
        return " (";
    }

    protected String getAutoIncrementClosing() {
        return ")";
    }

    protected String getAutoIncrementStartWithClause() {
        return "START WITH %d";
    }

    protected String getAutoIncrementByClause() {
        return "INCREMENT BY %d";
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
     * @see liquibase.database.Database#getLiquibaseTablespaceName()
     */
    public String getLiquibaseTablespaceName() {
        return liquibaseTablespaceName;
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

    /**
     * @see liquibase.database.Database#setLiquibaseTablespaceName(java.lang.String)
     */
    public void setLiquibaseTablespaceName(String tablespace) {
        this.liquibaseTablespaceName = tablespace;
    }

    /**
     * This method will check the database ChangeLog table used to keep track of
     * the changes in the file. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     *
     * @param updateExistingNullChecksums
     * @param contexts
     */
    public void checkDatabaseChangeLogTable(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, String... contexts) throws DatabaseException {
        Executor executor = ExecutorService.getInstance().getExecutor(this);

        Table changeLogTable = null;
        try {
            changeLogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl(Table.class, Column.class), this);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        List<SqlStatement> statementsToExecute = new ArrayList<SqlStatement>();

        boolean changeLogCreateAttempted = false;
        if (changeLogTable != null) {
            boolean hasDescription = changeLogTable.getColumn("DESCRIPTION") != null;
            boolean hasComments = changeLogTable.getColumn("COMMENTS") != null;
            boolean hasTag = changeLogTable.getColumn("TAG") != null;
            boolean hasLiquibase = changeLogTable.getColumn("LIQUIBASE") != null;
            boolean liquibaseColumnNotRightSize = false;
            if (!connection.getDatabaseProductName().equals("SQLite")) {
                Integer columnSize = changeLogTable.getColumn("LIQUIBASE").getType().getColumnSize();
                liquibaseColumnNotRightSize = columnSize != null && columnSize != 20;
            }
            boolean hasOrderExecuted = changeLogTable.getColumn("ORDEREXECUTED") != null;
            boolean checksumNotRightSize = false;
            if (!connection.getDatabaseProductName().equals("SQLite")) {
                Integer columnSize = changeLogTable.getColumn("MD5SUM").getType().getColumnSize();
                checksumNotRightSize = columnSize != null && columnSize != 35;
            }
            boolean hasExecTypeColumn = changeLogTable.getColumn("EXECTYPE") != null;

            if (!hasDescription) {
                executor.comment("Adding missing databasechangelog.description column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "DESCRIPTION", "VARCHAR(255)", null));
            }
            if (!hasTag) {
                executor.comment("Adding missing databasechangelog.tag column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "TAG", "VARCHAR(255)", null));
            }
            if (!hasComments) {
                executor.comment("Adding missing databasechangelog.comments column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "COMMENTS", "VARCHAR(255)", null));
            }
            if (!hasLiquibase) {
                executor.comment("Adding missing databasechangelog.liquibase column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LIQUIBASE", "VARCHAR(255)", null));
            }
            if (!hasOrderExecuted) {
                executor.comment("Adding missing databasechangelog.orderexecuted column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "ORDEREXECUTED", "INT", null));
                statementsToExecute.add(new UpdateStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()).addNewColumnValue("ORDEREXECUTED", -1));
                statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "ORDEREXECUTED", "INT", false));
            }
            if (checksumNotRightSize) {
                executor.comment("Modifying size of databasechangelog.md5sum column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "MD5SUM", "VARCHAR(35)"));
            }
            if (liquibaseColumnNotRightSize) {
                executor.comment("Modifying size of databasechangelog.liquibase column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LIQUIBASE", "VARCHAR(20)"));
            }
            if (!hasExecTypeColumn) {
                executor.comment("Adding missing databasechangelog.exectype column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "EXECTYPE", "VARCHAR(10)", null));
                statementsToExecute.add(new UpdateStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()).addNewColumnValue("EXECTYPE", "EXECUTED"));
                statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "EXECTYPE", "VARCHAR(10)", false));
            }

            List<Map> md5sumRS = ExecutorService.getInstance().getExecutor(this).queryForList(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(), "MD5SUM"));
            if (md5sumRS.size() > 0) {
                String md5sum = md5sumRS.get(0).get("MD5SUM").toString();
                if (!md5sum.startsWith(CheckSum.getCurrentVersion() + ":")) {
                    executor.comment("DatabaseChangeLog checksums are an incompatible version.  Setting them to null so they will be updated on next database update");
                    statementsToExecute.add(new RawSqlStatement("UPDATE " + escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM=null"));
                }
            }


        } else if (!changeLogCreateAttempted) {
            executor.comment("Create Database Change Log Table");
            SqlStatement createTableStatement = new CreateDatabaseChangeLogTableStatement();
            if (!canCreateChangeLogTable()) {
                throw new DatabaseException("Cannot create " + escapeTableName(getDefaultCatalogName(), getDefaultSchemaName(), getDatabaseChangeLogTableName()) + " table for your database.\n\n" +
                        "Please construct it manually using the following SQL as a base and re-run Liquibase:\n\n" +
                        createTableStatement);
            }
            // If there is no table in the database for recording change history create one.
            statementsToExecute.add(createTableStatement);
            LogFactory.getLogger().info("Creating database history table with name: " + escapeTableName(getDefaultCatalogName(), getDefaultSchemaName(), getDatabaseChangeLogTableName()));
//                }
        }

        for (SqlStatement sql : statementsToExecute) {
            executor.execute(sql);
            this.commit();
        }

        if (updateExistingNullChecksums) {
            for (RanChangeSet ranChangeSet : this.getRanChangeSetList()) {
                if (ranChangeSet.getLastCheckSum() == null) {
                    ChangeSet changeSet = databaseChangeLog.getChangeSet(ranChangeSet);
                    if (changeSet != null && new ContextChangeSetFilter(contexts).accepts(changeSet) && new DbmsChangeSetFilter(this).accepts(changeSet)) {
                        LogFactory.getLogger().debug("Updating null or out of date checksum on changeSet " + changeSet + " to correct value");
                        executor.execute(new UpdateChangeSetChecksumStatement(changeSet));
                    }
                }
            }
            commit();
            this.ranChangeSetList = null;
        }
    }


    protected boolean canCreateChangeLogTable() throws DatabaseException {
        return true;
    }

    public void setCanCacheLiquibaseTableInfo(boolean canCacheLiquibaseTableInfo) {
        this.canCacheLiquibaseTableInfo = canCacheLiquibaseTableInfo;
        hasDatabaseChangeLogTable = false;
        hasDatabaseChangeLogLockTable = false;
    }

    public boolean hasDatabaseChangeLogTable() throws DatabaseException {
        if (hasDatabaseChangeLogTable) {
            return true;
        }
        boolean hasTable = false;
        try {
            hasTable = SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable(this);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        if (canCacheLiquibaseTableInfo) {
            hasDatabaseChangeLogTable = hasTable;
        }
        return hasTable;
    }

    public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {
        if (canCacheLiquibaseTableInfo && hasDatabaseChangeLogLockTable) {
            return true;
        }
        boolean hasTable = false;
        try {
            hasTable = SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogLockTable(this);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        if (canCacheLiquibaseTableInfo) {
            hasDatabaseChangeLogLockTable = hasTable;
        }
        return hasTable;
    }

    public String getLiquibaseCatalogName() {
        return liquibaseCatalogName == null ? getDefaultCatalogName() : liquibaseCatalogName;
    }

    public String getLiquibaseSchemaName() {
        return liquibaseSchemaName == null ? getDefaultSchemaName() : liquibaseSchemaName;
    }

    /**
     * This method will check the database ChangeLogLock table used to keep track of
     * if a machine is updating the database. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     */
    public void checkDatabaseChangeLogLockTable() throws DatabaseException {

        Executor executor = ExecutorService.getInstance().getExecutor(this);
        if (!hasDatabaseChangeLogLockTable()) {

            executor.comment("Create Database Lock Table");
            executor.execute(new CreateDatabaseChangeLogLockTableStatement());
            this.commit();
            LogFactory.getLogger().debug("Created database lock table with name: " + escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogLockTableName()));
            this.hasDatabaseChangeLogLockTable = true;
        }
    }

    public boolean isCaseSensitive() {
        if (connection != null) {
            try {
                return ((JdbcConnection) connection).getUnderlyingConnection().getMetaData().supportsMixedCaseIdentifiers();
            } catch (SQLException e) {
                LogFactory.getLogger().warning("Cannot determine case sensitivity from JDBC driver", e);
                return false;
            }
        }
        return false;
    }

    public boolean isReservedWord(String string) {
        return false;
    }

    /*
    * Check if given string starts with numeric values that may cause problems and should be escaped.
    */
    protected boolean startsWithNumeric(String objectName) {
        return objectName.matches("^[0-9].*");
    }

// ------- DATABASE OBJECT DROPPING METHODS ---- //

    /**
     * Drops all objects owned by the connected user.
     */
    public void dropDatabaseObjects(CatalogAndSchema schemaToDrop) throws LiquibaseException {
        try {
            DatabaseSnapshot snapshot = null;
            try {
                snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemaToDrop, this, new SnapshotControl());
            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }

            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(new JdbcDatabaseSnapshot(this), snapshot, new CompareControl(snapshot.getSnapshotControl().getTypesToInclude()));
            List<ChangeSet> changeSets = new DiffToChangeLog(diffResult, new DiffOutputControl(true, true, false)).generateChangeSets();

            final boolean reEnableFK = supportsForeignKeyDisable() && disableForeignKeyChecks();
            try {
                for (ChangeSet changeSet : changeSets) {
                    for (Change change : changeSet.getChanges()) {
                        SqlStatement[] sqlStatements = change.generateStatements(this);
                        for (SqlStatement statement : sqlStatements) {
                            ExecutorService.getInstance().getExecutor(this).execute(statement);
                        }

                    }
                }
            } finally {
                if (reEnableFK) {
                    enableForeignKeyChecks();
                }
            }

        } finally {
            this.commit();
        }
    }

    public boolean supportsDropTableCascadeConstraints() {
        return (this instanceof FirebirdDatabase
                || this instanceof SQLiteDatabase
                || this instanceof SybaseDatabase
                || this instanceof SybaseASADatabase
                || this instanceof PostgresDatabase
                || this instanceof OracleDatabase
        );
    }

    public boolean isSystemObject(DatabaseObject example) {
        if (example == null) {
            return false;
        }
        if (example.getSchema() != null && example.getSchema().getName() != null && example.getSchema().getName().equalsIgnoreCase("information_schema")) {
            return true;
        }
        if (example instanceof Table && getSystemTables().contains(example.getName())) {
            return true;
        }

        if (example instanceof View && getSystemViews().contains(example.getName())) {
            return true;
        }

        return false;
    }

    public boolean isSystemView(CatalogAndSchema schema, String viewName) {
        schema = correctSchema(schema);
        if ("information_schema".equalsIgnoreCase(schema.getSchemaName())) {
            return true;
        } else if (getSystemViews().contains(viewName)) {
            return true;
        }
        return false;
    }

    public boolean isLiquibaseObject(DatabaseObject object) {
        if (object instanceof Table) {
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object.getSchema(), new Schema(getLiquibaseCatalogName(), getLiquibaseSchemaName()), this)) {
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, new Table().setName(getDatabaseChangeLogTableName()), this)) {
                    return true;
                }
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, new Table().setName(getDatabaseChangeLogLockTableName()), this)) {
                    return true;
                }
            }
            return false;
        } else if (object instanceof Column) {
            return isLiquibaseObject(((Column) object).getRelation());
        } else if (object instanceof Index) {
            return isLiquibaseObject(((Index) object).getTable());
        } else if (object instanceof PrimaryKey) {
            return isLiquibaseObject(((PrimaryKey) object).getTable());
        }
        return false;
    }

    // ------- DATABASE TAGGING METHODS ---- //

    /**
     * Tags the database changelog with the given string.
     */
    public void tag(String tagString) throws DatabaseException {
        Executor executor = ExecutorService.getInstance().getExecutor(this);
        try {
            int totalRows = ExecutorService.getInstance().getExecutor(this).queryForInt(new SelectFromDatabaseChangeLogStatement("COUNT(*)"));
            if (totalRows == 0) {
                ChangeSet emptyChangeSet = new ChangeSet(String.valueOf(new Date().getTime()), "liquibase", false, false, "liquibase-internal", null, null, quotingStrategy);
                this.markChangeSetExecStatus(emptyChangeSet, ChangeSet.ExecType.EXECUTED);
            }

//            Timestamp lastExecutedDate = (Timestamp) this.getExecutor().queryForObject(createChangeToTagSQL(), Timestamp.class);
            executor.execute(new TagDatabaseStatement(tagString));
            this.commit();

            getRanChangeSetList().get(getRanChangeSetList().size() - 1).setTag(tagString);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean doesTagExist(String tag) throws DatabaseException {
        int count = ExecutorService.getInstance().getExecutor(this).queryForInt(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByTag(tag), "COUNT(*)"));
        return count > 0;
    }

    @Override
    public String toString() {
        if (getConnection() == null) {
            return getShortName() + " Database";
        }

        return getConnection().getConnectionUserName() + " @ " + getConnection().getURL() + (getDefaultSchemaName() == null ? "" : " (Default Schema: " + getDefaultSchemaName() + ")");
    }


    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        schema = correctSchema(schema);
        String definition = (String) ExecutorService.getInstance().getExecutor(this).queryForObject(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName), String.class);
        if (definition == null) {
            return null;
        }
        return CREATE_VIEW_AS_PATTERN.matcher(definition).replaceFirst("");
    }

    public String escapeTableName(String catalogName, String schemaName, String tableName) {
        return escapeObjectName(catalogName, schemaName, tableName, Table.class);
    }

    public String escapeObjectName(String catalogName, String schemaName, String objectName, Class<? extends DatabaseObject> objectType) {
//        CatalogAndSchema catalogAndSchema = this.correctSchema(catalogName, schemaName);
//        catalogName = catalogAndSchema.getCatalogName();
//        schemaName = catalogAndSchema.getSchemaName();

        if (catalogName == null && schemaName == null) {
            return escapeObjectName(objectName, objectType);
        }

        if (supportsSchemas()) {
            catalogName = StringUtils.trimToNull(catalogName);
            schemaName = StringUtils.trimToNull(schemaName);
            if (catalogName == null && schemaName == null) {
                return escapeObjectName(objectName, objectType);
            } else if (catalogName == null || !this.supportsCatalogInObjectName()) {
                return escapeObjectName(schemaName, Schema.class) + "." + escapeObjectName(objectName, objectType);
            } else {
                return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(schemaName, Schema.class) + "." + escapeObjectName(objectName, objectType);
            }
        } else if (supportsCatalogs()) {
            if (StringUtils.trimToNull(catalogName) != null) {
                return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
            } else if (StringUtils.trimToNull(schemaName) != null) { //they actually mean catalog name
                return escapeObjectName(schemaName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
            } else {
                return escapeObjectName(objectName, objectType);
            }

        } else {
            return escapeObjectName(objectName, objectType);
        }
    }

    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null || quotingStrategy == ObjectQuotingStrategy.LEGACY) {
            return objectName;
        } else if (objectName.contains("-") || startsWithNumeric(objectName) || isReservedWord(objectName)) {
            return quotingStartCharacter + objectName + quotingEndCharacter;
        } else if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) {
            return quotingStartCharacter + objectName + quotingEndCharacter;
        }
        return objectName;
    }

    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        return escapeObjectName(catalogName, schemaName, indexName, Index.class);
    }

    public String escapeSequenceName(String catalogName, String schemaName, String sequenceName) {
        return escapeObjectName(catalogName, schemaName, sequenceName, Sequence.class);
    }

    public String escapeConstraintName(String constraintName) {
        return escapeObjectName(constraintName, Index.class);
    }

    public String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName) {
        if (columnName.contains("(")) {
            return columnName;
        }

        return escapeObjectName(columnName, Column.class);
    }

    public String escapeColumnNameList(String columnNames) {
        StringBuffer sb = new StringBuffer();
        for (String columnName : columnNames.split(",")) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(escapeObjectName(columnName.trim(), Column.class));
        }
        return sb.toString();

    }

    public boolean supportsSchemas() {
        return true;
    }

    public boolean supportsCatalogs() {
        return true;
    }

    public boolean jdbcCallsCatalogsSchemas() {
        return false;
    }

    public boolean supportsCatalogInObjectName() {
        return true;
    }

    public String generatePrimaryKeyName(String tableName) {
        return "PK_" + tableName.toUpperCase();
    }

    public String escapeViewName(String catalogName, String schemaName, String viewName) {
        return escapeObjectName(catalogName, schemaName, viewName, View.class);
    }

    /**
     * Returns the run status for the given ChangeSet
     */
    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        if (!hasDatabaseChangeLogTable()) {
            return ChangeSet.RunStatus.NOT_RAN;
        }

        RanChangeSet foundRan = getRanChangeSet(changeSet);

        if (foundRan == null) {
            return ChangeSet.RunStatus.NOT_RAN;
        } else {
            if (foundRan.getLastCheckSum() == null) {
                try {
                    LogFactory.getLogger().info("Updating NULL md5sum for " + changeSet.toString());
                    ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("UPDATE " + escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM='" + changeSet.generateCheckSum().toString() + "' WHERE ID='" + changeSet.getId() + "' AND AUTHOR='" + changeSet.getAuthor() + "' AND FILENAME='" + changeSet.getFilePath() + "'"));

                    this.commit();
                } catch (DatabaseException e) {
                    throw new DatabaseException(e);
                }

                return ChangeSet.RunStatus.ALREADY_RAN;
            } else {
                if (foundRan.getLastCheckSum().equals(changeSet.generateCheckSum())) {
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

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        if (!hasDatabaseChangeLogTable()) {
            return null;
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
    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        if (this.ranChangeSetList != null) {
            return this.ranChangeSetList;
        }

        String databaseChangeLogTableName = escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName());
        ranChangeSetList = new ArrayList<RanChangeSet>();
        if (hasDatabaseChangeLogTable()) {
            LogFactory.getLogger().info("Reading from " + databaseChangeLogTableName);
            SqlStatement select = new SelectFromDatabaseChangeLogStatement("FILENAME", "AUTHOR", "ID", "MD5SUM", "DATEEXECUTED", "ORDEREXECUTED", "TAG", "EXECTYPE", "DESCRIPTION", "COMMENTS").setOrderBy("DATEEXECUTED ASC", "ORDEREXECUTED ASC");
            List<Map> results = ExecutorService.getInstance().getExecutor(this).queryForList(select);
            for (Map rs : results) {
                String fileName = rs.get("FILENAME").toString();
                String author = rs.get("AUTHOR").toString();
                String id = rs.get("ID").toString();
                String md5sum = rs.get("MD5SUM") == null ? null : rs.get("MD5SUM").toString();
                String description = rs.get("DESCRIPTION") == null ? null : rs.get("DESCRIPTION").toString();
                String comments = rs.get("COMMENTS") == null ? null : rs.get("COMMENTS").toString();
                Object tmpDateExecuted = rs.get("DATEEXECUTED");
                Date dateExecuted = null;
                if (tmpDateExecuted instanceof Date) {
                    dateExecuted = (Date) tmpDateExecuted;
                } else {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        dateExecuted = df.parse((String) tmpDateExecuted);
                    } catch (ParseException e) {
                    }
                }
                String tag = rs.get("TAG") == null ? null : rs.get("TAG").toString();
                String execType = rs.get("EXECTYPE") == null ? null : rs.get("EXECTYPE").toString();
                try {
                    RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, CheckSum.parse(md5sum), dateExecuted, tag, ChangeSet.ExecType.valueOf(execType), description, comments);
                    ranChangeSetList.add(ranChangeSet);
                } catch (IllegalArgumentException e) {
                    LogFactory.getLogger().severe("Unknown EXECTYPE from database: " + execType);
                    throw e;
                }
            }
        }
        return ranChangeSetList;
    }

    public Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
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
    public void markChangeSetExecStatus(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {


        ExecutorService.getInstance().getExecutor(this).execute(new MarkChangeSetRanStatement(changeSet, execType));
        commit();
        getRanChangeSetList().add(new RanChangeSet(changeSet, execType));
    }

    public void removeRanStatus(ChangeSet changeSet) throws DatabaseException {

        ExecutorService.getInstance().getExecutor(this).execute(new RemoveChangeSetRanStatusStatement(changeSet));
        commit();

        getRanChangeSetList().remove(new RanChangeSet(changeSet));
    }

    public String escapeStringForDatabase(String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("'", "''");
    }

    public void commit() throws DatabaseException {
        try {
            getConnection().commit();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    public void rollback() throws DatabaseException {
        try {
            getConnection().rollback();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractJdbcDatabase that = (AbstractJdbcDatabase) o;

        if (connection == null) {
            if (that.connection == null) {
                return this == that;
            } else {
                return false;
            }
        } else {
            return connection.equals(that.connection);
        }
    }

    @Override
    public int hashCode() {
        return (connection != null ? connection.hashCode() : super.hashCode());
    }

    public void close() throws DatabaseException {
        DatabaseConnection connection = getConnection();
        if (connection != null) {
            if (previousAutoCommit != null) {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (DatabaseException e) {
                    LogFactory.getLogger().warning("Failed to restore the auto commit to " + previousAutoCommit);

                    throw e;
                }
            }
            connection.close();
        }
    }

    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    public boolean isAutoCommit() throws DatabaseException {
        try {
            return getConnection().getAutoCommit();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    public void setAutoCommit(boolean b) throws DatabaseException {
        try {
            getConnection().setAutoCommit(b);
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Default implementation, just look for "local" IPs. If the database returns a null URL we return false since we don't know it's safe to run the update.
     *
     * @throws liquibase.exception.DatabaseException
     *
     */
    public boolean isSafeToRunUpdate() throws DatabaseException {
        DatabaseConnection connection = getConnection();
        if (connection == null) {
            return true;
        }
        String url = connection.getURL();
        if (url == null) {
            return false;
        }
        return (url.contains("localhost")) || (url.contains("127.0.0.1"));
    }

    public void executeStatements(Change change, DatabaseChangeLog changeLog, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException {
        SqlStatement[] statements = change.generateStatements(this);

        execute(statements, sqlVisitors);
    }

    /*
     * Executes the statements passed as argument to a target {@link Database}
     *
     * @param statements an array containing the SQL statements to be issued
     * @param database the target {@link Database}
     * @throws DatabaseException if there were problems issuing the statements
     */
    public void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        for (SqlStatement statement : statements) {
            if (statement.skipOnUnsupported() && !SqlGeneratorFactory.getInstance().supports(statement, this)) {
                continue;
            }
            LogFactory.getLogger().debug("Executing Statement: " + statement);
            ExecutorService.getInstance().getExecutor(this).execute(statement, sqlVisitors);
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
        List<SqlVisitor> rollbackVisitors = new ArrayList<SqlVisitor>();
        if (sqlVisitors != null) {
            for (SqlVisitor visitor : sqlVisitors) {
                if (visitor.isApplyToRollback()) {
                    rollbackVisitors.add(visitor);
                }
            }
        }
        execute(statements, rollbackVisitors);
    }

    public void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        SqlStatement[] statements = change.generateRollbackStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(sql.getEndDelimiter()).append("\n\n");
            }
        }
    }

    public int getNextChangeSetSequenceValue() throws LiquibaseException {
        if (lastChangeSetSequenceValue == null) {
            if (getConnection() == null) {
                lastChangeSetSequenceValue = 0;
            } else {
                lastChangeSetSequenceValue = ExecutorService.getInstance().getExecutor(this).queryForInt(new GetNextChangeSetSequenceValueStatement());
            }
        }

        return ++lastChangeSetSequenceValue;
    }

    public List<DatabaseFunction> getDateFunctions() {
        return dateFunctions;
    }

    public boolean isFunction(String string) {
        if (string.endsWith("()")) {
            return true;
        }
        for (DatabaseFunction function : getDateFunctions()) {
            if (function.toString().equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    public void resetInternalState() {
        this.ranChangeSetList = null;
        this.hasDatabaseChangeLogLockTable = false;
    }

    public boolean supportsForeignKeyDisable() {
        return false;
    }

    public boolean disableForeignKeyChecks() throws DatabaseException {
        throw new DatabaseException("ForeignKeyChecks Management not supported");
    }

    public void enableForeignKeyChecks() throws DatabaseException {
        throw new DatabaseException("ForeignKeyChecks Management not supported");
    }

    public int getDataTypeMaxParameters(String dataTypeName) {
        return 2;
    }

    public CatalogAndSchema getSchemaFromJdbcInfo(String rawCatalogName, String rawSchemaName) {
        return this.correctSchema(new CatalogAndSchema(rawCatalogName, rawSchemaName));
    }

    public String getJdbcCatalogName(CatalogAndSchema schema) {
        return schema.getCatalogName();
    }

    public String getJdbcSchemaName(CatalogAndSchema schema) {
        return schema.getSchemaName();
    }

    public final String getJdbcCatalogName(Schema schema) {
        if (schema == null) {
            return getJdbcCatalogName(getDefaultSchema());
        } else {
            return getJdbcCatalogName(new CatalogAndSchema(schema.getCatalogName(), schema.getName()));
        }
    }

    public final String getJdbcSchemaName(Schema schema) {
        if (schema == null) {
            return getJdbcSchemaName(getDefaultSchema());
        } else {
            return getJdbcSchemaName(new CatalogAndSchema(schema.getCatalogName(), schema.getName()));
        }
    }

    public boolean dataTypeIsNotModifiable(final String typeName) {
        return unmodifiableDataTypes.contains(typeName.toLowerCase());
    }

    public void setObjectQuotingStrategy(ObjectQuotingStrategy quotingStrategy) {
        this.quotingStrategy = quotingStrategy;
    }

    public String generateDatabaseFunctionValue(final DatabaseFunction databaseFunction) {
        if (databaseFunction.getValue() == null) {
            return null;
        }
        if (isCurrentTimeFunction(databaseFunction.getValue().toLowerCase())) {
            return getCurrentDateTimeFunction();
        } else if (databaseFunction instanceof SequenceNextValueFunction) {
            if (sequenceNextValueFunction == null) {
                throw new RuntimeException(String.format("next value function for a sequence is not configured for database %s",
                        getDefaultDatabaseProductName()));
            }
            return String.format(sequenceNextValueFunction, databaseFunction.getValue());
        } else if (databaseFunction instanceof SequenceCurrentValueFunction) {
            if (sequenceCurrentValueFunction == null) {
                throw new RuntimeException(String.format("current value function for a sequence is not configured for database %s",
                        getDefaultDatabaseProductName()));
            }
            return String.format(sequenceCurrentValueFunction, databaseFunction.getValue());
        } else {
            return databaseFunction.getValue();
        }
    }

    private boolean isCurrentTimeFunction(String functionValue) {
        return functionValue.startsWith("current_timestamp")
                || functionValue.startsWith("current_datetime")
                || getCurrentDateTimeFunction().equalsIgnoreCase(functionValue);
    }

    public String getCurrentDateTimeFunction() {
        return currentDateTimeFunction;
    }
}
