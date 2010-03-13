package liquibase.database;

import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.change.core.*;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.structure.*;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.*;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.*;
import liquibase.util.ISODateFormat;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * AbstractDatabase is extended by all supported databases as a facade to the underlying database.
 * The physical connection can be retrieved from the AbstractDatabase implementation, as well as any
 * database-specific characteristics such as the datatype for "boolean" fields.
 */
public abstract class AbstractDatabase implements Database {

    private DatabaseConnection connection;
    private String defaultSchemaName;

    protected String currentDateTimeFunction;

	// List of Database native functions.
	protected List<DatabaseFunction> databaseFunctions;

    private List<RanChangeSet> ranChangeSetList;

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private String databaseChangeLogTableName = System.getProperty("liquibase.databaseChangeLogTableName") == null ? "DatabaseChangeLog".toUpperCase() : System.getProperty("liquibase.databaseChangeLogTableName");
    private String databaseChangeLogLockTableName = System.getProperty("liquibase.databaseChangeLogLockTableName") == null ? "DatabaseChangeLogLock".toUpperCase() : System.getProperty("liquibase.databaseChangeLogLockTableName");

    private Integer lastChangeSetSequenceValue;

    private boolean canCacheLiquibaseTableInfo = false;
    private boolean hasDatabaseChangeLogTable = false;
    private boolean hasDatabaseChangeLogLockTable = false;

    protected AbstractDatabase() {
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
        this.connection = conn;
        try {
            connection.setAutoCommit(getAutoCommitMode());
        } catch (DatabaseException sqle) {
            LogFactory.getLogger().warning("Can not set auto commit to " + getAutoCommitMode() + " on connection");
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
            return connection.getDatabaseProductName();
        } catch (DatabaseException e) {
            throw new RuntimeException("Cannot get database name");
        }
    }


    public String getDatabaseProductVersion() throws DatabaseException {
        try {
            return connection.getDatabaseProductVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    public int getDatabaseMajorVersion() throws DatabaseException {
        try {
            return connection.getDatabaseMajorVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    public int getDatabaseMinorVersion() throws DatabaseException {
        try {
            return connection.getDatabaseMinorVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    public String getDefaultCatalogName() throws DatabaseException {
        return null;
    }

    protected String getDefaultDatabaseSchemaName() throws DatabaseException {
        return getConnection().getConnectionUserName();
    }

    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    public void setDefaultSchemaName(String schemaName) throws DatabaseException {
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

    /**
     * This method will check the database ChangeLog table used to keep track of
     * the changes in the file. If the table does not exist it will create one
     * otherwise it will not do anything besides outputting a log message.
     */
    public void checkDatabaseChangeLogTable() throws DatabaseException {
        Executor executor = ExecutorService.getInstance().getExecutor(this);

        Table changeLogTable = DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(this).getDatabaseChangeLogTable(this);

        List<SqlStatement> statementsToExecute = new ArrayList<SqlStatement>();

        boolean changeLogCreateAttempted = false;
        if (changeLogTable != null) {
            boolean hasDescription = changeLogTable.getColumn("DESCRIPTION") != null;
            boolean hasComments = changeLogTable.getColumn("COMMENTS") != null;
            boolean hasTag = changeLogTable.getColumn("TAG") != null;
            boolean hasLiquibase = changeLogTable.getColumn("LIQUIBASE") != null;
            boolean liquibaseColumnNotRightSize = changeLogTable.getColumn("LIQUIBASE").getColumnSize() != 20;
            boolean hasOrderExecuted = changeLogTable.getColumn("ORDEREXECUTED") != null;
            boolean checksumNotRightSize = changeLogTable.getColumn("MD5SUM").getColumnSize() != 35;
            boolean hasExecTypeColumn = changeLogTable.getColumn("EXECTYPE") != null;

            if (!hasDescription) {
                executor.comment("Adding missing databasechangelog.description column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "DESCRIPTION", "VARCHAR(255)", null));
            }
            if (!hasTag) {
                executor.comment("Adding missing databasechangelog.tag column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "TAG", "VARCHAR(255)", null));
            }
            if (!hasComments) {
                executor.comment("Adding missing databasechangelog.comments column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "COMMENTS", "VARCHAR(255)", null));
            }
            if (!hasLiquibase) {
                executor.comment("Adding missing databasechangelog.liquibase column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LIQUIBASE", "VARCHAR(255)", null));
            }
            if (!hasOrderExecuted) {
                executor.comment("Adding missing databasechangelog.orderexecuted column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "ORDEREXECUTED", "INT", null, new UniqueConstraint()));
                statementsToExecute.add(new UpdateStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName()).addNewColumnValue("ORDEREXECUTED", -1));
                statementsToExecute.add(new SetNullableStatement(getLiquibaseSchemaName(),  getDatabaseChangeLogTableName(), "ORDEREXECUTED", "INT", false));
            }
            if (checksumNotRightSize) {
                executor.comment("Modifying size of databasechangelog.md5sum column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "MD5SUM", "VARCHAR(35)", true));
            }
            if (liquibaseColumnNotRightSize) {
                executor.comment("Modifying size of databasechangelog.liquibase column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LIQUIBASE", "VARCHAR(20)", true));
            }
            if (!hasExecTypeColumn) {
                executor.comment("Adding missing databasechangelog.exectype column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "EXECTYPE", "VARCHAR(10)", null, new UniqueConstraint()));
                statementsToExecute.add(new UpdateStatement(getLiquibaseSchemaName(), getDatabaseChangeLogTableName()).addNewColumnValue("EXECTYPE", "EXECUTED"));
                statementsToExecute.add(new SetNullableStatement(getLiquibaseSchemaName(),  getDatabaseChangeLogTableName(), "EXECTYPE", "VARCHAR(10)", false));
            }

            List<Map> md5sumRS = ExecutorService.getInstance().getExecutor(this).queryForList(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(), "MD5SUM"));
            if (md5sumRS.size() > 0) {
                String md5sum = md5sumRS.get(0).get("MD5SUM").toString();
                if (!md5sum.startsWith(CheckSum.getCurrentVersion() + ":")) {
                    executor.comment("DatabaseChangeLog checksums are an incompatible version.  Setting them to null so they will be updated on next database update");
                    statementsToExecute.add(new RawSqlStatement("UPDATE " + escapeTableName(getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM=null"));
                }
            }


        } else if (!changeLogCreateAttempted) {
            executor.comment("Create Database Change Log Table");
            SqlStatement createTableStatement = new CreateDatabaseChangeLogTableStatement();
            if (!canCreateChangeLogTable()) {
                throw new DatabaseException("Cannot create " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()) + " table for your database.\n\n" +
                        "Please construct it manually using the following SQL as a base and re-run LiquiBase:\n\n" +
                        createTableStatement);
            }
            // If there is no table in the database for recording change history create one.
            statementsToExecute.add(createTableStatement);
            LogFactory.getLogger().info("Creating database history table with name: " + escapeTableName(getDefaultSchemaName(), getDatabaseChangeLogTableName()));
//                }
        }

        for (SqlStatement sql : statementsToExecute) {
            executor.execute(sql);
            this.commit();
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
        boolean hasTable = DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(this).hasDatabaseChangeLogTable(this);
        if (canCacheLiquibaseTableInfo) {
            hasDatabaseChangeLogTable = hasTable;
        }
        return hasTable;
    }

    public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {
        if (hasDatabaseChangeLogLockTable) {
            return true;
        }
        boolean hasTable = DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(this).hasDatabaseChangeLogLockTable(this);
        if (canCacheLiquibaseTableInfo) {
            hasDatabaseChangeLogLockTable = hasTable;
        }
        return hasTable;
    }

    public String getLiquibaseSchemaName() {
        return getDefaultSchemaName();
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
            LogFactory.getLogger().debug("Created database lock table with name: " + escapeTableName(getLiquibaseSchemaName(), getDatabaseChangeLogLockTableName()));
        }
    }

// ------- DATABASE OBJECT DROPPING METHODS ---- //

    /**
     * Drops all objects owned by the connected user.
     *
     * @param schema
     */
    public void dropDatabaseObjects(String schema) throws DatabaseException {
        try {
            DatabaseSnapshot snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(this, schema, new HashSet<DiffStatusListener>());

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

//            for (Index index : snapshotGenerator.getIndexes()) {
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
                dropChanges.add(new AnonymousChange(new ClearDatabaseChangeLogTableStatement(schema)));
            }

            for (Change change : dropChanges) {
                for (SqlStatement statement : change.generateStatements(this)) {
                    ExecutorService.getInstance().getExecutor(this).execute(statement);
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
    public void tag(String tagString) throws DatabaseException {
        Executor executor = ExecutorService.getInstance().getExecutor(this);
        try {
            int totalRows = ExecutorService.getInstance().getExecutor(this).queryForInt(new SelectFromDatabaseChangeLogStatement("COUNT(*)"));
            if (totalRows == 0) {
                ChangeSet emptyChangeSet = new ChangeSet(String.valueOf(new Date().getTime()), "liquibase", false, false, "liquibase-internal", "liquibase-internal", null, null);
                this.markChangeSetExecStatus(emptyChangeSet, ChangeSet.ExecType.EXECUTED);
            }

//            Timestamp lastExecutedDate = (Timestamp) this.getExecutor().queryForObject(createChangeToTagSQL(), Timestamp.class);
            int rowsUpdated = executor.update(new TagDatabaseStatement(tagString));
            if (rowsUpdated == 0) {
                throw new DatabaseException("Did not tag database change log correctly");
            }
            this.commit();

            getRanChangeSetList().get(getRanChangeSetList().size() - 1).setTag(tagString);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean doesTagExist(String tag) throws DatabaseException {
        int count = ExecutorService.getInstance().getExecutor(this).queryForInt(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByTag("tag"), "COUNT(*)"));
        return count > 0;
    }

    @Override
    public String toString() {
        if (getConnection() == null) {
            return getTypeName() + " Database";
        }

        return getConnection().getConnectionUserName() + " @ " + getConnection().getURL() + (getDefaultSchemaName() == null ? "" : " (Default Schema: " + getDefaultSchemaName() + ")");
    }


    public boolean shouldQuoteValue(String value) {
        return true;
    }

    public String getViewDefinition(String schemaName, String viewName) throws DatabaseException {
        if (schemaName == null) {
            schemaName = convertRequestedSchemaToSchema(null);
        }
        String definition = (String) ExecutorService.getInstance().getExecutor(this).queryForObject(new GetViewDefinitionStatement(schemaName, viewName), String.class);
        if (definition == null) {
            return null;
        }
        return CREATE_VIEW_AS_PATTERN.matcher(definition).replaceFirst("");
    }

    public String escapeTableName(String schemaName, String tableName) {
        if (schemaName == null) {
            schemaName = getDefaultSchemaName();
        }

        if (StringUtils.trimToNull(schemaName) == null || !supportsSchemas()) {
            return escapeDatabaseObject(tableName);
        } else {
            return escapeDatabaseObject(schemaName) + "." + escapeDatabaseObject(tableName);
        }
    }

    public String escapeDatabaseObject(String objectName) {
        return objectName;
    }

    public String escapeIndexName(String schemaName, String indexName) {
        if (StringUtils.trimToNull(schemaName) == null || !supportsSchemas()) {
            return escapeDatabaseObject(indexName);
        } else {
            return escapeDatabaseObject(schemaName) + "." + escapeDatabaseObject(indexName);
        }
    }

    public String escapeSequenceName(String schemaName, String sequenceName) {
        if (schemaName == null) {
            schemaName = getDefaultSchemaName();
        }

        if (StringUtils.trimToNull(schemaName) == null || !supportsSchemas()) {
            return escapeDatabaseObject(sequenceName);
        } else {
            return escapeDatabaseObject(schemaName) + "." + escapeDatabaseObject(sequenceName);
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
        for (String columnName : columnNames.split(",")) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(escapeDatabaseObject(columnName.trim()));
        }
        return sb.toString();

    }

    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
        if (getDefaultCatalogName() == null) {
            return null;
        } else {
            if (requestedSchema == null) {
                return getDefaultCatalogName();
            }
            return StringUtils.trimToNull(requestedSchema);
        }
    }

    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
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
                    ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("UPDATE " + escapeTableName(getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM='" + changeSet.generateCheckSum().toString() + "' WHERE ID='" + changeSet.getId() + "' AND AUTHOR='" + changeSet.getAuthor() + "' AND FILENAME='" + changeSet.getFilePath() + "'"));

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
    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        if (this.ranChangeSetList != null) {
            return this.ranChangeSetList;
        }

        String databaseChangeLogTableName = escapeTableName(getLiquibaseSchemaName(), getDatabaseChangeLogTableName());
        ranChangeSetList = new ArrayList<RanChangeSet>();
        if (hasDatabaseChangeLogTable()) {
            LogFactory.getLogger().info("Reading from " + databaseChangeLogTableName);
            SqlStatement select = new SelectFromDatabaseChangeLogStatement("FILENAME", "AUTHOR", "ID", "MD5SUM", "DATEEXECUTED", "ORDEREXECUTED", "TAG").setOrderBy("DATEEXECUTED ASC", "ORDEREXECUTED ASC");
            List<Map> results = ExecutorService.getInstance().getExecutor(this).queryForList(select);
            for (Map rs : results) {
                String fileName = rs.get("FILENAME").toString();
                String author = rs.get("AUTHOR").toString();
                String id = rs.get("ID").toString();
                String md5sum = rs.get("MD5SUM") == null ? null : rs.get("MD5SUM").toString();
                Date dateExecuted = (Date) rs.get("DATEEXECUTED");
                String tag = rs.get("TAG") == null ? null : rs.get("TAG").toString();
                RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, CheckSum.parse(md5sum), dateExecuted, tag);
                ranChangeSetList.add(ranChangeSet);
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

        getRanChangeSetList().add(new RanChangeSet(changeSet));
    }

    public void removeRanStatus(ChangeSet changeSet) throws DatabaseException {

        ExecutorService.getInstance().getExecutor(this).execute(new RemoveChangeSetRanStatusStatement(changeSet));
        commit();

        getRanChangeSetList().remove(new RanChangeSet(changeSet));
    }

    public String escapeStringForDatabase(String string) {
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

        AbstractDatabase that = (AbstractDatabase) o;

        return !(connection != null ? !connection.equals(that.connection) : that.connection != null);

    }

    @Override
    public int hashCode() {
        return (connection != null ? connection.hashCode() : 0);
    }

    public void close() throws DatabaseException {
        try {
            DatabaseConnection connection = getConnection();
            if (connection != null) {
                connection.close();
            }
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
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
     * Default implementation, just look for "local" IPs
     *
     * @throws liquibase.exception.DatabaseException
     *
     */
    public boolean isLocalDatabase() throws DatabaseException {
        DatabaseConnection connection = getConnection();
        if (connection == null) {
            return true;
        }
        String url = connection.getURL();
        return (url.indexOf("localhost") >= 0) || (url.indexOf("127.0.0.1") >= 0);
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

    public Table getTable(String schemaName, String tableName) throws DatabaseException {
        return DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(this).getTable(schemaName, tableName, this);
    }

	public List<DatabaseFunction> getDatabaseFunctions() {
		return databaseFunctions;
	}

	public void setDatabaseFunctions(List<DatabaseFunction> databaseFunctions) {
		this.databaseFunctions = databaseFunctions;
	}
}
