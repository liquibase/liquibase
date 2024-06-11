package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawCallStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtil;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Encapsulates PostgreSQL database support.
 */
public class PostgresDatabase extends AbstractJdbcDatabase {
    private static final String dbFullVersion = null;
    public static final String PRODUCT_NAME = "PostgreSQL";
    public static final int MINIMUM_DBMS_MAJOR_VERSION = 9;
    public static final int MINIMUM_DBMS_MINOR_VERSION = 2;
    /**
     * The data type names which are valid for auto-increment columns.
     */
    public static final List<String> VALID_AUTO_INCREMENT_COLUMN_TYPE_NAMES = Collections.unmodifiableList(Arrays.asList("int2", "int4", "int8", "smallint", "int", "bigint", "smallserial", "serial", "bigserial"));

    /**
     * Maximum length of PostgresSQL identifier.
     * For details:
     * @see <a href="https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS">SQL-SYNTAX-IDENTIFIERS</a>.
     */
    static final int PGSQL_PK_BYTES_LIMIT = 63;
    static final String PGSQL_PK_SUFFIX = "_pkey";

    private static final int PGSQL_DEFAULT_TCP_PORT_NUMBER = 5432;
    private static final Logger LOG = Scope.getCurrentScope().getLog(PostgresDatabase.class);

    private final Set<String> systemTablesAndViews = new HashSet<>();

    private final Set<String> reservedWords = new HashSet<>();

    public PostgresDatabase() {
        super.setCurrentDateTimeFunction("NOW()");
        // "Reserved" or "reserved (can be function or type)" in PostgreSQL
        // from https://www.postgresql.org/docs/9.6/static/sql-keywords-appendix.html
        reservedWords.addAll(Arrays.asList("ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC",
                "ASYMMETRIC", "AUTHORIZATION", "BINARY", "BOTH", "CASE", "CAST", "CHECK", "COLLATE", "COLLATION",
                "COLUMN", "CONCURRENTLY", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_CATALOG", "CURRENT_DATE",
                "CURRENT_ROLE", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT",
                "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE", "END", "EXCEPT", "FALSE", "FETCH", "FOR", "FOREIGN",
                "FREEZE", "FROM", "FULL", "GRANT", "GROUP", "HAVING", "ILIKE", "IN", "INITIALLY", "INNER", "INTERSECT",
                "INTO", "IS", "ISNULL", "JOIN", "LATERAL", "LEADING", "LEFT", "LIKE", "LIMIT", "LOCALTIME",
                "LOCALTIMESTAMP", "NATURAL", "NOT", "NOTNULL", "NULL", "OFFSET", "ON", "ONLY", "OR", "ORDER", "OUTER",
                "OVERLAPS", "PLACING", "PRIMARY", "REFERENCES", "RETURNING", "RIGHT", "SELECT", "SESSION_USER",
                "SIMILAR", "SOME", "SYMMETRIC", "TABLE", "TABLESAMPLE", "THEN", "TO", "TRAILING", "TRUE", "UNION",
                "UNIQUE", "USER", "USING", "VARIADIC", "VERBOSE", "WHEN", "WHERE", "WINDOW", "WITH"));
        super.sequenceNextValueFunction = "nextval('%s')";
        super.sequenceCurrentValueFunction = "currval('%s')";
        super.unmodifiableDataTypes.addAll(Arrays.asList("bool", "int4", "int8", "float4", "float8", "bigserial", "serial", "oid", "bytea", "date", "timestamptz", "text", "int2[]", "int4[]", "int8[]", "float4[]", "float8[]", "bool[]", "varchar[]", "text[]", "numeric[]"));
        super.unquotedObjectsAreUppercased = false;
    }

    @Override
    public boolean equals(Object o) {
        // Actually, we don't need and more specific checks than the base method. This exists just to make SONAR happy.
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Actually, we don't need and more specific hashing than the base method. This exists just to make SONAR happy.
        return super.hashCode();
    }

    @Override
    public String getShortName() {
        return "postgresql";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "PostgreSQL";
    }

    @Override
    public Integer getDefaultPort() {
        return PGSQL_DEFAULT_TCP_PORT_NUMBER;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        if (!PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName())) {
            return false;
        }

        int majorVersion = conn.getDatabaseMajorVersion();
        int minorVersion = conn.getDatabaseMinorVersion();

        if ((majorVersion < MINIMUM_DBMS_MAJOR_VERSION) || ((majorVersion == MINIMUM_DBMS_MAJOR_VERSION) &&
                (minorVersion < MINIMUM_DBMS_MINOR_VERSION))) {
            LOG.warning(
                    String.format(
                            "Your PostgreSQL software version (%d.%d) seems to indicate that your software is " +
                                    "older than %d.%d. This means that you might encounter strange behaviour and " +
                                    "incorrect error messages.", majorVersion, minorVersion, MINIMUM_DBMS_MAJOR_VERSION, MINIMUM_DBMS_MINOR_VERSION));
            return true;
        }

        return true;
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }
        return null;
    }

    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public String getDatabaseChangeLogTableName() {
        return super.getDatabaseChangeLogTableName().toLowerCase(Locale.US);
    }

    @Override
    public String getDatabaseChangeLogLockTableName() {
        return super.getDatabaseChangeLogLockTableName().toLowerCase(Locale.US);
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);


        if (conn instanceof JdbcConnection) {
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = ((JdbcConnection) conn).createStatement();
                resultSet = statement.executeQuery("select setting from pg_settings where name = 'edb_redwood_date'");
                if (resultSet.next()) {
                    String setting = resultSet.getString(1);
                    if ("on".equals(setting)) {
                        LOG.warning("EnterpriseDB " + conn.getURL() + " does not store DATE columns. Instead, it auto-converts " +
                                "them " +
                                "to TIMESTAMPs. (edb_redwood_date=true)");
                    }
                }
            } catch (SQLException | DatabaseException e) {
                LOG.info("Cannot check pg_settings", e);
            } finally {
                JdbcUtil.close(resultSet, statement);
            }
        }

    }

    @Override
    public String unescapeDataTypeName(String dataTypeName) {
        return dataTypeName.replace("\"", "");
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        // All tables in the schemas pg_catalog and pg_toast are definitely system tables.
        if
        (
                (example instanceof Table)
                        && (example.getSchema() != null)
                        && (
                        ("pg_catalog".equals(example.getSchema().getName()))
                                || ("pg_toast".equals(example.getSchema().getName()))
                )
        ) {
            return true;
        }

        return super.isSystemObject(example);
    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public String getAutoIncrementClause() {
        if (useSerialDatatypes()) {
            return "";
        }

        return super.getAutoIncrementClause();
    }

    /**
     * Should the database use "serial" datatypes vs. "generated by default as identity"
     */
    public boolean useSerialDatatypes() {
        try {
            return getDatabaseMajorVersion() < 10;
        } catch (DatabaseException e) {
            return true;
        }
    }

    @Override
    protected String getAutoIncrementClause(final String generationType, final Boolean defaultOnNull) {
        if (useSerialDatatypes()) {
            return "";
        }

        if (StringUtil.isEmpty(generationType)) {
            return super.getAutoIncrementClause();
        }

        String autoIncrementClause = "GENERATED %s AS IDENTITY"; // %s -- [ ALWAYS | BY DEFAULT ]
        return String.format(autoIncrementClause, generationType);
    }

    @Override
    public boolean generateAutoIncrementStartWith(BigInteger startWith) {
        if (useSerialDatatypes()) {
            return false;
        }
        return super.generateAutoIncrementStartWith(startWith);
    }

    @Override
    public boolean generateAutoIncrementBy(BigInteger incrementBy) {
        if (useSerialDatatypes()) {
            return false;
        }

        return super.generateAutoIncrementBy(incrementBy);
    }

    /**
     * This has special case logic to handle NOT quoting column names if they are
     * of type 'LiquibaseColumn' - columns in the DATABASECHANGELOG or DATABASECHANGELOGLOCK
     * tables.
     */
    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if ((quotingStrategy == ObjectQuotingStrategy.LEGACY) && hasMixedCase(objectName)) {
            return "\"" + objectName + "\"";
        } else if (objectType != null && LiquibaseColumn.class.isAssignableFrom(objectType)) {
            return (objectName != null && !objectName.isEmpty()) ? objectName.trim() : objectName;
        }

        return super.escapeObjectName(objectName, objectType);
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if ((objectName == null) || (quotingStrategy != ObjectQuotingStrategy.LEGACY)) {
            return super.correctObjectName(objectName, objectType);
        }
        //
        // Check preserve case flag for schema
        //
        if (objectType.equals(Schema.class) && Boolean.TRUE.equals(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())) {
            return objectName;
        }

        if (objectName.contains("-")
                || hasMixedCase(objectName)
                || startsWithNumeric(objectName)
                || isReservedWord(objectName)) {
            return objectName;
        } else {
            return objectName.toLowerCase(Locale.US);
        }
    }

    /*
     * Check if given string has case problems according to postgresql documentation.
     * If there are at least one characters with upper case while all other are in lower case (or vice versa) this
     * string should be escaped.
     *
     * Note: This may make postgres support more case sensitive than normally is, but needs to be left in for backwards
     * compatibility.
     * Method is public so a subclass extension can override it to always return false.
     */
    protected boolean hasMixedCase(String tableName) {
        if (tableName == null) {
            return false;
        }
        return StringUtil.hasUpperCase(tableName) && StringUtil.hasLowerCase(tableName);
    }

    @Override
    public boolean isReservedWord(String tableName) {
        return reservedWords.contains(tableName.toUpperCase());
    }

    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawCallStatement("select current_schema()");
    }

    /**
     * Generates PK following {@code PostgreSQL} conventions:
     * <li>Postgres PK size is limited with 63 bytes.
     * <li>Postgres PK is suffixed with '_pkey'.
     *
     * @param tableName Table name as the base name for the generated PK.
     * @return PK name.
     */
    @Override
    public String generatePrimaryKeyName(final String tableName) {
        final Charset charset = GlobalConfiguration.FILE_ENCODING.getCurrentValue();

        final byte[] tableNameBytes = tableName.getBytes(charset);
        final int pkNameBaseAllowedBytesCount = PGSQL_PK_BYTES_LIMIT - PGSQL_PK_SUFFIX.getBytes(charset).length;

        if (tableNameBytes.length <= pkNameBaseAllowedBytesCount) {
            return tableName + PGSQL_PK_SUFFIX;
        }

        // As symbols could be encoded with more than 1 byte, the last symbol bytes couldn't be identified precisely.
        // To avoid the last symbol being the invalid one, just truncate it.
        final String baseName = new String(tableNameBytes, 0, pkNameBaseAllowedBytesCount, charset);
        return baseName.substring(0, baseName.length() - 1) + PGSQL_PK_SUFFIX;
    }

    @Override
    @SuppressWarnings("squid:S109")
    public int getMaxFractionalDigitsForTimestamp() {

        int major = 0;
        int minor = 0;

        try {
            major = getDatabaseMajorVersion();
            minor = getDatabaseMinorVersion();
        } catch (DatabaseException x) {
            Scope.getCurrentScope().getLog(getClass()).warning(
                    "Unable to determine exact database server version"
                            + " - specified TIMESTAMP precision"
                            + " will not be set: ", x);
            return 0;
        }

        // PostgreSQL 7.2 introduced fractional support...
        // https://www.postgresql.org/docs/9.2/static/datatype-datetime.html
        String minimumVersion = "7.2";

        if (StringUtil.isMinimumVersion(minimumVersion, major, minor, 0)) {
            return 6;
        } else {
            return 0;
        }
    }

    @Override
    public CatalogAndSchema.CatalogAndSchemaCase getSchemaAndCatalogCase() {
        return CatalogAndSchema.CatalogAndSchemaCase.LOWER_CASE;
    }

    @Override
    public void rollback() throws DatabaseException {
        super.rollback();

        // Rollback in postgresql resets the search path. Need to put it back to the defaults
        // Prevent resetting the search path if we are running in a mode that does not update the database to avoid spurious 
        // SET SEARCH_PATH SQL statements
        final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this);
        if(executor.updatesDatabase()) {
            DatabaseUtils.initializeDatabase(getDefaultCatalogName(), getDefaultSchemaName(), this);
        }
    }

    @Override
    public void setDefaultCatalogName(String defaultCatalogName) {
        if (StringUtil.isNotEmpty(defaultCatalogName)) {
            Scope.getCurrentScope().getUI().sendMessage("WARNING: Postgres does not support catalogs, so the values set in 'defaultCatalogName' and 'referenceDefaultCatalogName' will be ignored.");
        }
        super.setDefaultCatalogName(defaultCatalogName);
    }

    @Override
    public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
        return type.isAssignableFrom(Table.class);
    }

    @Override
    public boolean supportsDatabaseChangeLogHistory() {
        return true;
    }
}
