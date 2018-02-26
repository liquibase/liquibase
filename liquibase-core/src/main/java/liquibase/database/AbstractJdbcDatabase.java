package liquibase.database;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.change.core.DropTableChange;
import liquibase.changelog.*;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.*;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.RawCallStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.SQLException;
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

    private static final Pattern startsWithNumberPattern = Pattern.compile("^[0-9].*");
    private static final int FETCH_SIZE = 1000;
    private static final int DEFAULT_MAX_TIMESTAMP_FRACTIONAL_DIGITS = 9;
    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private final Set<String> reservedWords = new HashSet<>();
    protected String defaultCatalogName;
    protected String defaultSchemaName;
    protected String currentDateTimeFunction;
    /**
     * The sequence name will be substituted into the string e.g. NEXTVAL('%s')
     */
    protected String sequenceNextValueFunction;
    protected String sequenceCurrentValueFunction;

    // List of Database native functions.
    protected List<DatabaseFunction> dateFunctions = new ArrayList<>();
    protected List<String> unmodifiableDataTypes = new ArrayList<>();
    protected BigInteger defaultAutoIncrementStartWith = BigInteger.ONE;
    protected BigInteger defaultAutoIncrementBy = BigInteger.ONE;
    // most databases either lowercase or uppercase unuqoted objects such as table and column names.
    protected Boolean unquotedObjectsAreUppercased;
    // whether object names should be quoted
    protected ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.LEGACY;
    protected Boolean caseSensitive;
    private String databaseChangeLogTableName;
    private String databaseChangeLogLockTableName;
    private String liquibaseTablespaceName;
    private String liquibaseSchemaName;
    private String liquibaseCatalogName;
    private Boolean previousAutoCommit;
    private boolean canCacheLiquibaseTableInfo = false;
    private DatabaseConnection connection;
    private boolean outputDefaultSchema = true;
    private boolean outputDefaultCatalog = true;

    private boolean defaultCatalogSet;

    private Map<String, Object> attributes = new HashMap<>();

    public String getName() {
        return toString();
    }

    @Override
    public boolean requiresPassword() {
        return true;
    }

    @Override
    public boolean requiresUsername() {
        return true;
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    // ------- DATABASE INFORMATION METHODS ---- //

    @Override
    public DatabaseConnection getConnection() {
        return connection;
    }

    @Override
    public void setConnection(final DatabaseConnection conn) {
        LogService.getLog(getClass()).debug(LogType.LOG, "Connected to " + conn.getConnectionUserName() + "@" + conn.getURL());
        this.connection = conn;
        try {
            boolean autoCommit = conn.getAutoCommit();
            if (autoCommit == getAutoCommitMode()) {
                // Don't adjust the auto-commit mode if it's already what the database wants it to be.
                LogService.getLog(getClass()).debug(LogType.LOG, "Not adjusting the auto commit mode; it is already " + autoCommit);
            } else {
                // Store the previous auto-commit mode, because the connection needs to be restored to it when this
                // AbstractDatabase type is closed. This is important for systems which use connection pools.
                previousAutoCommit = autoCommit;

                LogService.getLog(getClass()).debug(LogType.LOG, "Setting auto commit to " + getAutoCommitMode() + " from " + autoCommit);
                connection.setAutoCommit(getAutoCommitMode());

            }
        } catch (DatabaseException e) {
            LogService.getLog(getClass()).warning(LogType.LOG, "Cannot set auto commit to " + getAutoCommitMode() + " on connection");
        }

        this.connection.attached(this);
    }

    @Override
    public boolean getAutoCommitMode() {
        return !supportsDDLInTransaction();
    }

    @Override
    public final void addReservedWords(Collection<String> words) {
        reservedWords.addAll(words);
    }

    /**
     * Determines if the database supports DDL within a transaction or not.
     *
     * @return True if the database supports DDL within a transaction, otherwise false.
     */
    // TODO this might be a dangerous default value. I would rather make this an abstract method and have every
    // implementation specify it explicitly.
    @Override
    public boolean supportsDDLInTransaction() {
        return true;
    }

    @Override
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


    @Override
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

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        if (connection == null) {
            return 999;
        }
        try {
            return connection.getDatabaseMajorVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
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

    @Override
    public String getDefaultCatalogName() {
        if (defaultCatalogName == null) {
            if ((defaultSchemaName != null) && !this.supportsSchemas()) {
                return defaultSchemaName;
            }

            if (connection != null) {
                try {
                    defaultCatalogName = getConnectionCatalogName();
                } catch (DatabaseException e) {
                    LogService.getLog(getClass()).info(LogType.LOG, "Error getting default catalog", e);
                }
            }
        }
        return defaultCatalogName;
    }

    @Override
    public void setDefaultCatalogName(final String defaultCatalogName) {
        this.defaultCatalogName = correctObjectName(defaultCatalogName, Catalog.class);
        defaultCatalogSet = defaultCatalogName != null;

    }

    protected String getConnectionCatalogName() throws DatabaseException {
        return connection.getCatalog();
    }

    @Deprecated
    public CatalogAndSchema correctSchema(final String catalog, final String schema) {
        return new CatalogAndSchema(catalog, schema).standardize(this);
    }

    @Deprecated
    @Override
    public CatalogAndSchema correctSchema(final CatalogAndSchema schema) {
        if (schema == null) {
            return new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName());
        }

        return schema.standardize(this);
    }

    @Override
    public String correctObjectName(final String objectName, final Class<? extends DatabaseObject> objectType) {
        if ((quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) || (unquotedObjectsAreUppercased == null) ||
                ( objectName == null) || (objectName.startsWith(getQuotingStartCharacter()) && objectName.endsWith(getQuotingEndCharacter()))) {
            return objectName;
        } else if (Boolean.TRUE.equals(unquotedObjectsAreUppercased)) {
            return objectName.toUpperCase(Locale.US);
        } else {
            return objectName.toLowerCase(Locale.US);
        }
    }

    @Override
    public CatalogAndSchema getDefaultSchema() {
        return new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName());

    }

    @Override
    public String getDefaultSchemaName() {
        if (!supportsSchemas()) {
            return getDefaultCatalogName();
        }

        if ((defaultSchemaName == null) && (connection != null)) {
            defaultSchemaName = getConnectionSchemaName();
        }

        return defaultSchemaName;
    }

    @Override
    public Integer getDefaultScaleForNativeDataType(String nativeDataType) {
        // Default implementation does not return anything; this is up to the concrete implementation.
        return null;
    }

    @Override
    public void setDefaultSchemaName(final String schemaName) {
        this.defaultSchemaName = correctObjectName(schemaName, Schema.class);
        if (!supportsSchemas()) {
            defaultCatalogSet = schemaName != null;
        }
    }

    /**
     * Overwrite this method to get the default schema name for the connection.
     * If you only need to change the statement that obtains the current schema then override
     * @see AbstractJdbcDatabase#getConnectionSchemaNameCallStatement()
     */
    protected String getConnectionSchemaName() {
        if (connection == null) {
            return null;
        }
        if (connection instanceof OfflineConnection) {
            return ((OfflineConnection) connection).getSchema();
        }

        try {
            SqlStatement currentSchemaStatement = getConnectionSchemaNameCallStatement();
            return ExecutorService.getInstance().getExecutor(this).
                    queryForObject(currentSchemaStatement, String.class);
        } catch (Exception e) {
            LogService.getLog(getClass()).info(LogType.LOG, "Error getting default schema", e);
        }
        return null;
    }

    /**
     * Used to obtain the connection schema name through a statement
     * Override this method to change the statement.
     * Only override this if getConnectionSchemaName is left unchanges or is using this method.
     * @see AbstractJdbcDatabase#getConnectionSchemaName()
     */
    protected SqlStatement getConnectionSchemaNameCallStatement(){
        return new RawCallStatement("call current_schema");
    }

    @Override
    public Integer getFetchSize() {
        return FETCH_SIZE;
    }

    /**
     * Returns system (undroppable) views.
     */
    protected Set<String> getSystemTables() {
        return new HashSet<>();
    }


    /**
     * Returns system (undroppable) views.
     */
    protected Set<String> getSystemViews() {
        return new HashSet<>();
    }

    // ------- DATABASE FEATURE INFORMATION METHODS ---- //

    /**
     * Does the database type support sequence.
     */
    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return true;
    }

    // ------- DATABASE-SPECIFIC SQL METHODS ---- //

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
    @Override
    public String getDateLiteral(final String isoDate) {
        if (isDateOnly(isoDate) || isTimeOnly(isoDate)) {
            return "'" + isoDate + "'";
        } else if (isDateTime(isoDate)) {
            return "'" + isoDate.replace('T', ' ') + "'";
        } else {
            return "BAD_DATE_FORMAT:" + isoDate;
        }
    }

    @Override
    public String getDateTimeLiteral(final java.sql.Timestamp date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    @Override
    public String getDateLiteral(final java.sql.Date date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    @Override
    public String getTimeLiteral(final java.sql.Time date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    @Override
    public String getDateLiteral(final Date date) {
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

    @Override
    public Date parseDate(final String dateAsString) throws DateParseException {
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

    /***
     * Returns true if the String conforms to an ISO 8601 date, e.g. 2016-12-31
     * @param isoDate
     */
    protected boolean isDateOnly(final String isoDate) {
        return isoDate.matches("^\\d{4}\\-\\d{2}\\-\\d{2}$");
    }

    /***
     * Returns true if the String conforms to an ISO 8601 date
     * plus a time (hours, minutes, whole seconds and optionally fraction of a second) in UTC, e.g. 2016-12-31T18:43:59
     * The "T" may be replaced by a space.
     * CAUTION: Does NOT recognize values with a timezone information (...[+-Z]...)
     * @param isoDate
     */
    protected boolean isDateTime(final String isoDate) {
        return isoDate.matches("^\\d{4}\\-\\d{2}\\-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?$");
    }

    /***
     * Returns true if the String conforms to an ISO 8601 date
     * plus a timestamp (hours, minutes, seconds and at least one decimal fraction) in UTC,
     * e.g. 2016-12-31T18:43:59.3 or 2016-12-31T18:43:59.345
     * CAUTION: Does NOT recognize values with a timezone information (...[+-Z]...)
     * The "T" may be replaced by a space.
     * @param isoDate
     */
    protected boolean isTimestamp(final String isoDate) {
        return isoDate.matches("^\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+$");
    }

    /***
     * Returns true if the String conforms to an ISO 8601 time (hours, minutes and whole seconds) in UTC,
     * e.g. 18:43:59
     * CAUTION: Does NOT recognize values with a timezone information (...[+-Z]...)
     * @param isoDate
     */
    protected boolean isTimeOnly(final String isoDate) {
        return isoDate.matches("^\\d{2}:\\d{2}:\\d{2}$");
    }

    /**
     * Returns database-specific line comment string.
     */
    @Override
    public String getLineComment() {
        return "--";
    }

    @Override
    public String getAutoIncrementClause(final BigInteger startWith, final BigInteger incrementBy) {
        if (!supportsAutoIncrement()) {
            return "";
        }

        // generate an SQL:2003 STANDARD compliant auto increment clause by default

        String autoIncrementClause = getAutoIncrementClause();

        boolean generateStartWith = generateAutoIncrementStartWith(startWith);
        boolean generateIncrementBy = generateAutoIncrementBy(incrementBy);

        if (generateStartWith || generateIncrementBy) {
            autoIncrementClause += getAutoIncrementOpening();

            if (generateStartWith) {
                autoIncrementClause += String.format(getAutoIncrementStartWithClause(), (startWith == null) ? defaultAutoIncrementStartWith : startWith);
            }

            if (generateIncrementBy) {
                if (generateStartWith) {
                    autoIncrementClause += ", ";
                }

                autoIncrementClause += String.format(getAutoIncrementByClause(), (incrementBy == null) ? defaultAutoIncrementBy : incrementBy);
            }

            autoIncrementClause += getAutoIncrementClosing();
        }

        return autoIncrementClause;
    }

    protected String getAutoIncrementClause() {
        return "GENERATED BY DEFAULT AS IDENTITY";
    }

    protected boolean generateAutoIncrementStartWith(final BigInteger startWith) {
        return (startWith != null) && !startWith.equals(defaultAutoIncrementStartWith);
    }

    protected boolean generateAutoIncrementBy(final BigInteger incrementBy) {
        return (incrementBy != null) && !incrementBy.equals(defaultAutoIncrementBy);
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

    @Override
    public String getConcatSql(final String... values) {
        StringBuffer returnString = new StringBuffer();
        for (String value : values) {
            returnString.append(value).append(" || ");
        }

        return returnString.toString().replaceFirst(" \\|\\| $", "");
    }

    @Override
    public String getDatabaseChangeLogTableName() {
        if (databaseChangeLogTableName != null) {
            return databaseChangeLogTableName;
        }

        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDatabaseChangeLogTableName();
    }

    @Override
    public void setDatabaseChangeLogTableName(final String tableName) {
        this.databaseChangeLogTableName = tableName;
    }

    @Override
    public String getDatabaseChangeLogLockTableName() {
        if (databaseChangeLogLockTableName != null) {
            return databaseChangeLogLockTableName;
        }

        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDatabaseChangeLogLockTableName();
    }

    @Override
    public void setDatabaseChangeLogLockTableName(final String tableName) {
        this.databaseChangeLogLockTableName = tableName;
    }

    @Override
    public String getLiquibaseTablespaceName() {
        if (liquibaseTablespaceName != null) {
            return liquibaseTablespaceName;
        }

        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getLiquibaseTablespaceName();
    }

    @Override
    public void setLiquibaseTablespaceName(final String tablespace) {
        this.liquibaseTablespaceName = tablespace;
    }

    protected boolean canCreateChangeLogTable() throws DatabaseException {
        return ((StandardChangeLogHistoryService) ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this)).canCreateChangeLogTable();
    }

    @Override
    public void setCanCacheLiquibaseTableInfo(final boolean canCacheLiquibaseTableInfo) {
        this.canCacheLiquibaseTableInfo = canCacheLiquibaseTableInfo;
    }

    @Override
    public String getLiquibaseCatalogName() {
        if (liquibaseCatalogName != null) {
            return liquibaseCatalogName;
        }

        ConfigurationProperty configuration = LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration.LIQUIBASE_CATALOG_NAME);
        if (configuration.getWasOverridden()) {
            return configuration.getValue(String.class);
        }

        return getDefaultCatalogName();
    }

    @Override
    public void setLiquibaseCatalogName(final String catalogName) {
        this.liquibaseCatalogName = catalogName;
    }

    @Override
    public String getLiquibaseSchemaName() {
        if (liquibaseSchemaName != null) {
            return liquibaseSchemaName;
        }

        ConfigurationProperty configuration = LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration.LIQUIBASE_SCHEMA_NAME);
        if (configuration.getWasOverridden()) {
            return configuration.getValue(String.class);
        }

        return getDefaultSchemaName();
    }

    @Override
    public void setLiquibaseSchemaName(final String schemaName) {
        this.liquibaseSchemaName = schemaName;
    }

    @Override
    public boolean isCaseSensitive() {
        if (caseSensitive == null) {
            if ((connection != null) && (connection instanceof JdbcConnection)) {
                try {
                    caseSensitive = ((JdbcConnection) connection).getUnderlyingConnection().getMetaData().supportsMixedCaseIdentifiers();
                } catch (SQLException e) {
                    LogService.getLog(getClass()).warning(LogType.LOG, "Cannot determine case sensitivity from JDBC driver", e);
                }
            }
        }

        if (caseSensitive == null) {
            return false;
        } else {
            return caseSensitive.booleanValue();
        }
    }

    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean isReservedWord(final String string) {
        return reservedWords.contains(string.toUpperCase());
    }

    /*
    * Check if given string starts with numeric values that may cause problems and should be escaped.
    */
    protected boolean startsWithNumeric(final String objectName) {
        return startsWithNumberPattern.matcher(objectName).matches();
    }

    @Override
    public void dropDatabaseObjects(final CatalogAndSchema schemaToDrop) throws LiquibaseException {
        ObjectQuotingStrategy currentStrategy = this.getObjectQuotingStrategy();
        this.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        try {
            DatabaseSnapshot snapshot;
            try {
                final SnapshotControl snapshotControl = new SnapshotControl(this);
                final Set<Class<? extends DatabaseObject>> typesToInclude = snapshotControl.getTypesToInclude();

                //We do not need to remove indexes and primary/unique keys explicitly. They should be removed
                //as part of tables.
                typesToInclude.remove(Index.class);
                typesToInclude.remove(PrimaryKey.class);
                typesToInclude.remove(UniqueConstraint.class);

                if (supportsForeignKeyDisable()) {
                    //We do not remove ForeignKey because they will be disabled and removed as parts of tables.
                    typesToInclude.remove(ForeignKey.class);
                }

                final long createSnapshotStarted = System.currentTimeMillis();
                snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemaToDrop, this, snapshotControl);
                LogService.getLog(getClass()).debug(LogType.LOG, String.format("Database snapshot generated in %d ms. Snapshot includes: %s", System.currentTimeMillis() - createSnapshotStarted, typesToInclude));
            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }

            final long changeSetStarted = System.currentTimeMillis();
            CompareControl compareControl = new CompareControl(
                    new CompareControl.SchemaComparison[] {
                            new CompareControl.SchemaComparison(
                                    CatalogAndSchema.DEFAULT,
                                    schemaToDrop)},
                    snapshot.getSnapshotControl().getTypesToInclude());
            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(
                    new EmptyDatabaseSnapshot(this),
                    snapshot,
                    compareControl);

            List<ChangeSet> changeSets = new DiffToChangeLog(diffResult, new DiffOutputControl(true, true, false, null).addIncludedSchema(schemaToDrop)).generateChangeSets();
            LogService.getLog(getClass()).debug(LogType.LOG, String.format("ChangeSet to Remove Database Objects generated in %d ms.", System.currentTimeMillis() - changeSetStarted));

            boolean previousAutoCommit = this.getAutoCommitMode();
            this.commit(); //clear out currently executed statements
            this.setAutoCommit(false); //some DDL doesn't work in autocommit mode
            final boolean reEnableFK = supportsForeignKeyDisable() && disableForeignKeyChecks();
            try {
                for (ChangeSet changeSet : changeSets) {
                    changeSet.setFailOnError(false);
                    for (Change change : changeSet.getChanges()) {
                        if (change instanceof DropTableChange) {
                            ((DropTableChange) change).setCascadeConstraints(true);
                        }
                        SqlStatement[] sqlStatements = change.generateStatements(this);
                        for (SqlStatement statement : sqlStatements) {
                            ExecutorService.getInstance().getExecutor(this).execute(statement);
                        }

                    }
                    this.commit();
                }
            } finally {
                if (reEnableFK) {
                    enableForeignKeyChecks();
                }
            }

            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).destroy();
            LockServiceFactory.getInstance().getLockService(this).destroy();

            this.setAutoCommit(previousAutoCommit);
            LogService.getLog(getClass()).info(LogType.LOG, String.format("Successfully deleted all supported object types in schema %s.", schemaToDrop.toString()));
        } finally {
            this.setObjectQuotingStrategy(currentStrategy);
            this.commit();
        }
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return ((this instanceof SQLiteDatabase) || (this instanceof SybaseDatabase) || (this instanceof
            SybaseASADatabase) || (this instanceof PostgresDatabase) || (this instanceof OracleDatabase));
    }

    @Override
    public boolean isSystemObject(final DatabaseObject example) {
        if (example == null) {
            return false;
        }
        if ((example.getSchema() != null) && (example.getSchema().getName() != null) && "information_schema"
            .equalsIgnoreCase(example.getSchema().getName())) {
            return true;
        }
        if ((example instanceof Table) && getSystemTables().contains(example.getName())) {
            return true;
        }

        return (example instanceof View) && getSystemViews().contains(example.getName());

    }

    public boolean isSystemView(CatalogAndSchema schema, final String viewName) {
        schema = schema.customize(this);
        if ("information_schema".equalsIgnoreCase(schema.getSchemaName())) {
            return true;
        } else return getSystemViews().contains(viewName);
    }

    @Override
    public boolean isLiquibaseObject(final DatabaseObject object) {
        if (object instanceof Table) {
            Schema liquibaseSchema = new Schema(getLiquibaseCatalogName(), getLiquibaseSchemaName());
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, new Table().setName(getDatabaseChangeLogTableName()).setSchema(liquibaseSchema), null, this)) {
                return true;
            }
            return DatabaseObjectComparatorFactory.getInstance().isSameObject(object, new Table().setName(getDatabaseChangeLogLockTableName()).setSchema(liquibaseSchema), null, this);
        } else if (object instanceof Column) {
            return isLiquibaseObject(((Column) object).getRelation());
        } else if (object instanceof Index) {
            return isLiquibaseObject(((Index) object).getTable());
        } else if (object instanceof PrimaryKey) {
            return isLiquibaseObject(((PrimaryKey) object).getTable());
        }
        return false;
    }

    @Override
    public void tag(final String tagString) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).tag(tagString);
    }

    @Override
    public boolean doesTagExist(final String tag) throws DatabaseException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).tagExists(tag);
    }

    @Override
    public String toString() {
        if (getConnection() == null) {
            return getShortName() + " Database";
        }

        return getConnection().getConnectionUserName() + " @ " + getConnection().getURL() + (getDefaultSchemaName() == null ? "" : " (Default Schema: " + getDefaultSchemaName() + ")");
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, final String viewName) throws DatabaseException {
        schema = schema.customize(this);
        String definition = ExecutorService.getInstance().getExecutor(this).queryForObject(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName), String.class);
        if (definition == null) {
            return null;
        }
        return CREATE_VIEW_AS_PATTERN.matcher(definition).replaceFirst("");
    }

    @Override
    public String escapeTableName(final String catalogName, final String schemaName, final String tableName) {
        return escapeObjectName(catalogName, schemaName, tableName, Table.class);
    }

    @Override
    public String escapeObjectName(String catalogName, String schemaName, final String objectName,
                                   final Class<? extends DatabaseObject> objectType) {
        if (supportsSchemas()) {
            catalogName = StringUtils.trimToNull(catalogName);
            schemaName = StringUtils.trimToNull(schemaName);

            if (catalogName == null) {
                catalogName = this.getDefaultCatalogName();
            }
            if (schemaName == null) {
                schemaName = this.getDefaultSchemaName();
            }

            if (!supportsCatalogInObjectName(objectType)) {
                catalogName = null;
            }
            if ((catalogName == null) && (schemaName == null)) {
                return escapeObjectName(objectName, objectType);
            } else if ((catalogName == null) || !this.supportsCatalogInObjectName(objectType)) {
                if (isDefaultSchema(catalogName, schemaName) && !getOutputDefaultSchema()) {
                    return escapeObjectName(objectName, objectType);
                } else {
                    return escapeObjectName(schemaName, Schema.class) + "." + escapeObjectName(objectName, objectType);
                }
            } else {
                if (isDefaultSchema(catalogName, schemaName) && !getOutputDefaultSchema() && !getOutputDefaultCatalog
                        ()) {
                    return escapeObjectName(objectName, objectType);
                } else if (isDefaultSchema(catalogName, schemaName) && !getOutputDefaultCatalog()) {
                    return escapeObjectName(schemaName, Schema.class) + "." + escapeObjectName(objectName, objectType);
                } else {
                    return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(schemaName, Schema.class) + "." + escapeObjectName(objectName, objectType);
                }
            }
        } else if (supportsCatalogs()) {
            catalogName = StringUtils.trimToNull(catalogName);
            schemaName = StringUtils.trimToNull(schemaName);

            if (catalogName != null) {
                if (getOutputDefaultCatalog()) {
                    return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                } else {
                    if (!defaultCatalogSet && isDefaultCatalog(catalogName)) {
                        return escapeObjectName(objectName, objectType);
                    } else {
                        return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                    }
                }
            } else {

                //they actually mean catalog name
                if (schemaName != null) {
                    if (getOutputDefaultCatalog()) {
                        return escapeObjectName(schemaName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                    } else {
                        if (!defaultCatalogSet && isDefaultCatalog(schemaName)) {
                            return escapeObjectName(objectName, objectType);
                        } else {
                            return escapeObjectName(schemaName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                        }
                    }
                } else {
                    catalogName = this.getDefaultCatalogName();

                    if (catalogName == null) {
                        return escapeObjectName(objectName, objectType);
                    } else {
                        if (defaultCatalogSet || (isDefaultCatalog(catalogName) && getOutputDefaultCatalog())) {
                            return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                        } else {
                            return escapeObjectName(objectName, objectType);
                        }
                    }
                }
            }

        } else {
            return escapeObjectName(objectName, objectType);
        }
    }

    @Override
    public String escapeObjectName(String objectName, final Class<? extends DatabaseObject> objectType) {
        if (objectName != null) {
            objectName = objectName.trim();
            if (mustQuoteObjectName(objectName, objectType)) {
                return quoteObject(objectName, objectType);
            } else if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) {
                return quoteObject(objectName, objectType);
            }
        }
        return objectName;
    }

    protected boolean mustQuoteObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return objectName.contains("-") || startsWithNumeric(objectName) || isReservedWord(objectName) || objectName.matches(".*\\W.*");
    }

    protected String getQuotingStartCharacter() {
        return "\"";
    }

    protected String getQuotingEndCharacter() {
        return "\"";
    }

    protected String getQuotingEndReplacement() {
        return "\"\"";
    }

    public String quoteObject(final String objectName, final Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }
        return getQuotingStartCharacter() + objectName.replace(getQuotingEndCharacter(), getQuotingEndReplacement()) + getQuotingEndCharacter();
    }

    @Override
    public String escapeIndexName(final String catalogName, final String schemaName, final String indexName) {
        return escapeObjectName(catalogName, schemaName, indexName, Index.class);
    }

    @Override
    public String escapeSequenceName(final String catalogName, final String schemaName, final String sequenceName) {
        return escapeObjectName(catalogName, schemaName, sequenceName, Sequence.class);
    }

    @Override
    public String escapeConstraintName(final String constraintName) {
        return escapeObjectName(constraintName, Index.class);
    }

    @Override
    public String escapeColumnName(final String catalogName, final String schemaName, final String tableName, final String columnName) {
        return escapeObjectName(columnName, Column.class);
    }

    @Override
    public String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName, boolean quoteNamesThatMayBeFunctions) {
        if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) {
            return quoteObject(columnName, Column.class);
        }

        if (columnName.contains("(")) {
            if (quoteNamesThatMayBeFunctions) {
                return quoteObject(columnName, Column.class);
            } else {
                return columnName;
            }
        }
        return escapeObjectName(columnName, Column.class);
    }

    @Override
    public String escapeColumnNameList(final String columnNames) {
        StringBuilder sb = new StringBuilder();
        for (String columnName : StringUtils.splitAndTrim(columnNames, ",")) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            boolean descending = false;
            if (columnName.matches("(?i).*\\s+DESC")) {
                columnName = columnName.replaceFirst("(?i)\\s+DESC$", "");
                descending = true;
            } else if (columnName.matches("(?i).*\\s+ASC")) {
                columnName = columnName.replaceFirst("(?i)\\s+ASC$", "");
            }
            sb.append(escapeObjectName(columnName, Column.class));
            if (descending) {
                sb.append(" DESC");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean supportsSchemas() {
        return true;
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    public boolean jdbcCallsCatalogsSchemas() {
        return false;
    }

    @Override
    public boolean supportsCatalogInObjectName(final Class<? extends DatabaseObject> type) {
        return false;
    }

    @Override
    public String generatePrimaryKeyName(final String tableName) {
        return "PK_" + tableName.toUpperCase(Locale.US);
    }

    @Override
    public String escapeViewName(final String catalogName, final String schemaName, final String viewName) {
        return escapeObjectName(catalogName, schemaName, viewName, View.class);
    }

    @Override
    public ChangeSet.RunStatus getRunStatus(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRunStatus(changeSet);
    }

    @Override
    public RanChangeSet getRanChangeSet(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanChangeSet(changeSet);
    }

    @Override
    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanChangeSets();
    }

    @Override
    public Date getRanDate(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanDate(changeSet);
    }

    @Override
    public void markChangeSetExecStatus(final ChangeSet changeSet, final ChangeSet.ExecType execType) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).setExecType(changeSet, execType);
    }

    @Override
    public void removeRanStatus(final ChangeSet changeSet) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).removeFromHistory(changeSet);
    }

    @Override
    public String escapeStringForDatabase(final String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("'", "''");
    }

    @Override
    public void commit() throws DatabaseException {
        try {
            getConnection().commit();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void rollback() throws DatabaseException {
        try {
            getConnection().rollback();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

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
        return ((connection != null) ? connection.hashCode() : super.hashCode());
    }

    @Override
    public void close() throws DatabaseException {
        ExecutorService.getInstance().clearExecutor(this);
        DatabaseConnection connection = getConnection();
        if (connection != null) {
            if (previousAutoCommit != null) {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (DatabaseException e) {
                    LogService.getLog(getClass()).warning(LogType.LOG, "Failed to restore the auto commit to " + previousAutoCommit);

                    throw e;
                }
            }
            connection.close();
        }
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    @Override
    public boolean isAutoCommit() throws DatabaseException {
        try {
            return getConnection().getAutoCommit();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setAutoCommit(final boolean b) throws DatabaseException {
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
    @Override
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

    @Override
    public void executeStatements(final Change change, final DatabaseChangeLog changeLog, final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        SqlStatement[] statements = change.generateStatements(this);

        execute(statements, sqlVisitors);
    }

    /*
     * Executes the statements passed
     *
     * @param statements an array containing the SQL statements to be issued
     * @param sqlVisitors a list of {@link SqlVisitor} objects to be applied to the executed statements
     * @throws DatabaseException if there were problems issuing the statements
     */
    @Override
    public void execute(final SqlStatement[] statements, final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        for (SqlStatement statement : statements) {
            if (statement.skipOnUnsupported() && !SqlGeneratorFactory.getInstance().supports(statement, this)) {
                continue;
            }
            LogService.getLog(getClass()).debug(LogType.LOG, "Executing Statement: " + statement);
            try {
                ExecutorService.getInstance().getExecutor(this).execute(statement, sqlVisitors);
            } catch (DatabaseException e) {
                if (statement.continueOnError()) {
                    LogService.getLog(getClass()).severe(LogType.LOG, "Error executing statement '"+statement.toString()+"', but continuing", e);
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public void saveStatements(final Change change, final List<SqlVisitor> sqlVisitors, final Writer writer) throws
        IOException {
        SqlStatement[] statements = change.generateStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(sql.getEndDelimiter()).append(StreamUtil.getLineSeparator()).append(StreamUtil.getLineSeparator());
            }
        }
    }

    @Override
    public void executeRollbackStatements(final SqlStatement[] statements, final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        execute(statements, filterRollbackVisitors(sqlVisitors));
    }

    @Override
    public void executeRollbackStatements(final Change change, final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        final SqlStatement[] statements = change.generateRollbackStatements(this);
        executeRollbackStatements(statements, sqlVisitors);
    }

    @Override
    public void saveRollbackStatement(final Change change, final List<SqlVisitor> sqlVisitors, final Writer writer) throws IOException, LiquibaseException {
        SqlStatement[] statements = change.generateRollbackStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(sql.getEndDelimiter()).append("\n\n");
            }
        }
    }

    /**
     * Takes a list of SqlVisitors and returns a new list with only the SqlVisitors set to apply to rollbacks
     */
    protected List<SqlVisitor> filterRollbackVisitors(final List<SqlVisitor> visitors) {
        final List<SqlVisitor> rollbackVisitors = new ArrayList<>();
        if (visitors != null) {
            for (SqlVisitor visitor : visitors) {
                if (visitor.isApplyToRollback()) {
                    rollbackVisitors.add(visitor);
                }
            }
        }

        return rollbackVisitors;
    }

    @Override
    public List<DatabaseFunction> getDateFunctions() {
        return dateFunctions;
    }

    @Override
    public boolean isFunction(final String string) {
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

    @Override
    public void resetInternalState() {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).reset();
        LockServiceFactory.getInstance().getLockService(this).reset();
    }

    @Override
    public boolean supportsForeignKeyDisable() {
        return false;
    }

    @Override
    public boolean disableForeignKeyChecks() throws DatabaseException {
        throw new DatabaseException("ForeignKeyChecks Management not supported");
    }

    @Override
    public void enableForeignKeyChecks() throws DatabaseException {
        throw new DatabaseException("ForeignKeyChecks Management not supported");
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return false;
    }

    @Override
    public int getDataTypeMaxParameters(final String dataTypeName) {
        return 2;
    }

    public CatalogAndSchema getSchemaFromJdbcInfo(final String rawCatalogName, final String rawSchemaName) {
        return new CatalogAndSchema(rawCatalogName, rawSchemaName).customize(this);
    }

    public String getJdbcCatalogName(final CatalogAndSchema schema) {
        return correctObjectName(schema.getCatalogName(), Catalog.class);
    }

    public String getJdbcSchemaName(final CatalogAndSchema schema) {
        return correctObjectName(schema.getSchemaName(), Schema.class);
    }

    public final String getJdbcCatalogName(final Schema schema) {
        if (schema == null) {
            return getJdbcCatalogName(getDefaultSchema());
        } else {
            return getJdbcCatalogName(new CatalogAndSchema(schema.getCatalogName(), schema.getName()));
        }
    }

    public final String getJdbcSchemaName(final Schema schema) {
        if (schema == null) {
            return getJdbcSchemaName(getDefaultSchema());
        } else {
            return getJdbcSchemaName(new CatalogAndSchema(schema.getCatalogName(), schema.getName()));
        }
    }

    @Override
    public boolean dataTypeIsNotModifiable(final String typeName) {
        return unmodifiableDataTypes.contains(typeName.toLowerCase());
    }

    @Override
    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return this.quotingStrategy;
    }

    @Override
    public void setObjectQuotingStrategy(final ObjectQuotingStrategy quotingStrategy) {
        this.quotingStrategy = quotingStrategy;
    }

    @Override
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
            String sequenceName = databaseFunction.getValue();
            String sequenceSchemaName = ((SequenceNextValueFunction) databaseFunction).getSequenceSchemaName();

            sequenceName = escapeObjectName(null, sequenceSchemaName, sequenceName, Sequence.class);
            if (sequenceNextValueFunction.contains("'")) {
                /* For PostgreSQL, the quotes around dangerous identifiers (e.g. mixed-case) need to stay in place,
                 * or else PostgreSQL will not be able to find the sequence. */
                if (! (this instanceof PostgresDatabase)) {
                    sequenceName = sequenceName.replace("\"", "");
                }
            }

            return String.format(sequenceNextValueFunction, sequenceName);
        } else if (databaseFunction instanceof SequenceCurrentValueFunction) {
            if (sequenceCurrentValueFunction == null) {
                throw new RuntimeException(String.format("current value function for a sequence is not configured for database %s",
                        getDefaultDatabaseProductName()));
            }

            String sequenceSchemaName = ((SequenceCurrentValueFunction) databaseFunction).getSequenceSchemaName();
            String sequenceName = databaseFunction.getValue();
            sequenceName = escapeObjectName(null, sequenceSchemaName, sequenceName, Sequence.class);

            if (sequenceCurrentValueFunction.contains("'")) {
                /* For PostgreSQL, the quotes around dangerous identifiers (e.g. mixed-case) need to stay in place,
                 * or else PostgreSQL will not be able to find the sequence. */
                if (! (this instanceof PostgresDatabase)) {
                    sequenceName = sequenceName.replace("\"", "");
                }
            }
            return String.format(sequenceCurrentValueFunction, sequenceName);
        } else {
            return databaseFunction.getValue();
        }
    }

    private boolean isCurrentTimeFunction(final String functionValue) {
        if (functionValue == null) {
            return false;
        }

        return functionValue.startsWith("current_timestamp")
                || functionValue.startsWith("current_datetime")
                || functionValue.equalsIgnoreCase(getCurrentDateTimeFunction());
    }

    @Override
    public String getCurrentDateTimeFunction() {
        return currentDateTimeFunction;
    }

    @Override
    public void setCurrentDateTimeFunction(final String function) {
        if (function != null) {
            this.currentDateTimeFunction = function;
            this.dateFunctions.add(new DatabaseFunction(function));
        }
    }

    @Override
    public boolean isDefaultSchema(final String catalog, final String schema) {
        if (!supportsSchemas()) {
            return true;
        }

        if (!isDefaultCatalog(catalog)) {
            return false;
        }
        return (schema == null) || schema.equalsIgnoreCase(getDefaultSchemaName());
    }

    @Override
    public boolean isDefaultCatalog(final String catalog) {
        if (!supportsCatalogs()) {
            return true;
        }

        return (catalog == null) || catalog.equalsIgnoreCase(getDefaultCatalogName());

    }

    @Override
    public boolean getOutputDefaultSchema() {
        return outputDefaultSchema;
    }

    @Override
    public void setOutputDefaultSchema(final boolean outputDefaultSchema) {
        this.outputDefaultSchema = outputDefaultSchema;

    }

    @Override
    public boolean getOutputDefaultCatalog() {
        return outputDefaultCatalog;
    }

    @Override
    public void setOutputDefaultCatalog(final boolean outputDefaultCatalog) {
        this.outputDefaultCatalog = outputDefaultCatalog;
    }

    @Override
    public boolean supportsPrimaryKeyNames() {
        return true;
    }

    @Override
    public String getSystemSchema(){
        return "information_schema";
    }

    @Override
    public String escapeDataTypeName(String dataTypeName) {
        return dataTypeName;
    }

    @Override
    public String unescapeDataTypeName(String dataTypeName) {
        return dataTypeName;
    }

    @Override
    public String unescapeDataTypeString(String dataTypeString) {
        return dataTypeString;
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public AbstractJdbcDatabase set(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
        return this;
    }

    @Override
    public ValidationErrors validate() {
        return new ValidationErrors();
    }

    /**
     * Most relational databases support 9 fractional digits, and subclasses must overwrite this method if they
     * support less than that.
     *
     * @return the maxmimum number of supported fractional digits in TIMESTAMP columns
     */
    @Override
    public int getMaxFractionalDigitsForTimestamp() {
        if (getConnection() == null) {
            // if no connection is there we cannot do anything...
            LogService.getLog(getClass()).warning(
                    LogType.LOG, "No database connection available - specified"
                            + " DATETIME/TIMESTAMP precision will be tried");
            return DEFAULT_MAX_TIMESTAMP_FRACTIONAL_DIGITS;
        }

        return DEFAULT_MAX_TIMESTAMP_FRACTIONAL_DIGITS;
    }

    /**
     * SQL Standard (Foundation) says: "...if <timestamp precision> is not specified, then 6 is implicit."
     *
     * @return the default precision / number of maximum digits in a timestamp if nothing else is specified.
     */
    @Override
    public int getDefaultFractionalDigitsForTimestamp() {
        return ((getMaxFractionalDigitsForTimestamp() >= 6) ? 6 : getMaxFractionalDigitsForTimestamp());
    }

    @Override
    public boolean supportsBatchUpdates() throws DatabaseException {
        if (connection instanceof OfflineConnection) {
            return false;
        } else if (connection instanceof JdbcConnection) {
            return ((JdbcConnection)getConnection()).supportsBatchUpdates();
        } else {
            // Normally, the connection can only be one of the two above types. But if, for whatever reason, it is
            // not, let's err on the safe side.
            return false;
        }
    }

    @Override
    public boolean supportsNotNullConstraintNames() {
        return false;
    }

    @Override
    public boolean requiresExplicitNullForColumns() {
        return false;
    }


    /**
     * This logic is used when db support catalogs
     * @return UPPER_CASE by default
     */
    @Override
    public CatalogAndSchema.CatalogAndSchemaCase getSchemaAndCatalogCase() {
        return CatalogAndSchema.CatalogAndSchemaCase.UPPER_CASE;
    }
}
