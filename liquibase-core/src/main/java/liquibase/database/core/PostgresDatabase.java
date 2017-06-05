package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawCallStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Encapsulates PostgreSQL database support.
 */
public class PostgresDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "PostgreSQL";
    public static final int MINIMUM_DBMS_MAJOR_VERSION = 9;
    public static final int MINIMUM_DBMS_MINOR_VERSION = 2;

    private Set<String> systemTablesAndViews = new HashSet<String>();

    private Set<String> reservedWords = new HashSet<String>();

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
        super.unmodifiableDataTypes.addAll(Arrays.asList("bool", "int4", "int8", "float4", "float8", "bigserial",
                "serial", "oid", "bytea", "timestamptz", "text"));
        super.unquotedObjectsAreUppercased=false;
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
        return 5432;
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
        if (! PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName()))
            return false;

        int majorVersion = conn.getDatabaseMajorVersion();
        int minorVersion =conn.getDatabaseMinorVersion();

        if (majorVersion < MINIMUM_DBMS_MAJOR_VERSION ||
                (majorVersion == MINIMUM_DBMS_MAJOR_VERSION && minorVersion < MINIMUM_DBMS_MINOR_VERSION)) {
            LogFactory.getInstance().getLog().warning(
            String.format("Your PostgreSQL software version (%d.%d) seems to indicate that your software is older than " +
                            "%d.%d. Unfortunately, this is not supported, and this connection cannot be used. Sorry.",
                    majorVersion, minorVersion, majorVersion, minorVersion));
            return false;
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
        return super.getDatabaseChangeLogTableName().toLowerCase();
    }

    @Override
    public String getDatabaseChangeLogLockTableName() {
        return super.getDatabaseChangeLogLockTableName().toLowerCase();
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);

        Logger log = LogFactory.getInstance().getLog();

        if (conn instanceof JdbcConnection) {
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = ((JdbcConnection) conn).createStatement();
                resultSet = statement.executeQuery("select setting from pg_settings where name = 'edb_redwood_date'");
                if (resultSet.next()) {
                    String setting = resultSet.getString(1);
                    if (setting != null && setting.equals("on")) {
                        log.warning("EnterpriseDB "+conn.getURL()+" does not store DATE columns. Auto-converts them to TIMESTAMPs. (edb_redwood_date=true)");
                    }
                }
            } catch (Exception e) {
                log.info("Cannot check pg_settings", e);
            } finally {
                JdbcUtils.close(resultSet, statement);
            }
        }

    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (example instanceof Table) {
            if (example.getSchema() != null) {
                if ("pg_catalog".equals(example.getSchema().getName())
                        || "pg_toast".equals(example.getSchema().getName())) {
                    return true;
                }
            }
        }
        return super.isSystemObject(example);
    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public String getAutoIncrementClause() {
        return "";
    }

    @Override
    public boolean generateAutoIncrementStartWith(BigInteger startWith) {
        return false;
    }

    @Override
    public boolean generateAutoIncrementBy(BigInteger incrementBy) {
        return false;
    }

    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (quotingStrategy == ObjectQuotingStrategy.LEGACY && hasMixedCase(objectName)) {
            return "\"" + objectName + "\"";
        } else {
            return super.escapeObjectName(objectName, objectType);
        }
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null || quotingStrategy != ObjectQuotingStrategy.LEGACY) {
            return super.correctObjectName(objectName, objectType);
        }
        if (objectName.contains("-") || hasMixedCase(objectName) || startsWithNumeric(objectName) || isReservedWord(objectName)) {
            return objectName;
        } else {
            return objectName.toLowerCase();
        }
    }

    /*
    * Check if given string has case problems according to postgresql documentation.
    * If there are at least one characters with upper case while all other are in lower case (or vice versa) this string should be escaped.
    *
    * Note: This may make postgres support more case sensitive than normally is, but needs to be left in for backwards compatibility.
    * Method is public so a subclass extension can override it to always return false.
    */
    protected boolean hasMixedCase(String tableName) {
        if (tableName == null) {
            return false;
        }
        return StringUtils.hasUpperCase(tableName) && StringUtils.hasLowerCase(tableName);
    }

    @Override
    public boolean isReservedWord(String tableName) {
        return reservedWords.contains(tableName.toUpperCase());
    }

    /*
     * Get the current search paths
     */
    private List<String> getSearchPaths() {
        List<String> searchPaths = null;

        try {
            DatabaseConnection con = getConnection();

            if (con != null) {
                String searchPathResult = ExecutorService.getInstance().getExecutor(this).queryForObject(new RawSqlStatement("SHOW search_path"), String.class);

                if (searchPathResult != null) {
                    String dirtySearchPaths[] = searchPathResult.split("\\,");
                    searchPaths = new ArrayList<String>();
                    for (String searchPath : dirtySearchPaths) {
                        searchPath = searchPath.trim();

                        // Ensure there is consistency ..
                        if (searchPath.equals("\"$user\"")) {
                            searchPath = "$user";
                        }

                        searchPaths.add(searchPath);
                    }
                }

            }
        } catch (Exception e) {
            // TODO: Something?
            e.printStackTrace();
            LogFactory.getInstance().getLog().severe("Failed to get default catalog name from postgres", e);
        }

        return searchPaths;
    }

    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawCallStatement("select current_schema()");
    }

    private boolean catalogExists(String catalogName) throws DatabaseException {
        return catalogName != null && runExistsQuery(
                "select count(*) from information_schema.schemata where catalog_name='" + catalogName + "'");
    }

    private boolean schemaExists(String schemaName) throws DatabaseException {
        return schemaName != null && runExistsQuery("select count(*) from information_schema.schemata where schema_name='" + schemaName + "'");
    }

    private boolean runExistsQuery(String query) throws DatabaseException {
        Long count = ExecutorService.getInstance().getExecutor(this).queryForLong(new RawSqlStatement(query));

        return count != null && count > 0;
    }

    public int getMaxFractionalDigitsForTimestamp() {

        int major = 0;
        int minor = 0;
        int patch = 0;

        try {
            major = getDatabaseMajorVersion();
            minor = getDatabaseMinorVersion();
        } catch (DatabaseException x) {
            LogFactory.getInstance().getLog().warning(
                    "Unable to determine exact database server version"
                            + " - specified TIMESTAMP precision"
                            + " will not be set: ", x);
            return 0;
        }

        // PostgreSQL 7.2 introduced fractional support...
        // https://www.postgresql.org/docs/9.2/static/datatype-datetime.html
        String minimumVersion = "7.2";

        if (StringUtils.isMinimumVersion(minimumVersion, major, minor, patch))
            return 6;
        else
            return 0;
    }
}
