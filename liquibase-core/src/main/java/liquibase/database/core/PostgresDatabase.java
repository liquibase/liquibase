package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.logging.Logger;
import liquibase.structure.DatabaseObject;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawCallStatement;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Encapsulates PostgreSQL database support.
 */
public class PostgresDatabase extends AbstractJdbcDatabase {
    private static final Logger log = LogFactory.getInstance().getLog();
    public static final String PRODUCT_NAME = "PostgreSQL";

    private Set<String> systemTablesAndViews = new HashSet<String>();

    private Set<String> reservedWords = new HashSet<String>();

    public PostgresDatabase() {
        super.setCurrentDateTimeFunction("NOW()");
        //got list from http://www.postgresql.org/docs/9.1/static/sql-keywords-appendix.html?
        reservedWords.addAll(Arrays.asList("ALL","ANALYSE", "AND", "ANY","ARRAY","AS", "ASC","ASYMMETRIC", "AUTHORIZATION", "BINARY", "BOTH","CASE","CAST","CHECK", "COLLATE","COLLATION", "COLUMN","CONCURRENTLY", "CONSTRAINT", "CREATE", "CURRENT_CATALOG", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT", "DEFERRABLE", "DESC", "DISTINCT", "DO",
                "ELSE", "END", "EXCEPT", "FALSE", "FETCH", "FOR", "FOREIGN", "FROM", "FULL", "GRANT", "GROUP", "HAVING", "ILIKE", "IN", "INITIALLY", "INTERSECT", "INTO", "IS", "ISNULL", "JOIN", "LEADING", "LEFT", "LIKE", "LIMIT", "LITERAL", "LOCALTIME", "LOCALTIMESTAMP", "NOT", "NULL", "OFFSET", "ON", "ONLY", "OR", "ORDER", "OUTER", "OVER", "OVERLAPS",
                "PLACING", "PRIMARY", "REFERENCES", "RETURNING", "RIGHT", "SELECT", "SESSION_USER", "SIMILAR", "SOME", "SYMMETRIC", "TABLE", "THEN", "TO", "TRAILING", "TRUE", "UNION", "UNIQUE", "USER", "USING", "VARIADIC", "VERBOSE", "WHEN", "WHERE", "WINDOW", "WITH"));
        super.sequenceNextValueFunction = "nextval('%s')";
        super.sequenceCurrentValueFunction = "currval('%s')";
        super.unmodifiableDataTypes.addAll(Arrays.asList("bool", "int4", "int8", "float4", "float8", "bigserial", "serial", "bytea", "timestamptz", "text"));
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
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
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
    public void setConnection(DatabaseConnection databaseConnection) {
        setDefaultSchemaNameToSearchPathIfNeeded(databaseConnection);
        super.setConnection(databaseConnection);
        logWarnIfStoreDateColumnNotSupported(databaseConnection);
    }

    /**
     * This method adds defaultSchema to searchPath array if needed
     *
     * @param databaseConnection - this expected JDBC connection that will be used to execute queries
     */
    private void setDefaultSchemaNameToSearchPathIfNeeded(final DatabaseConnection databaseConnection) {
        if (!(databaseConnection instanceof JdbcConnection)) {
            return;
        }
        String schema = ((JdbcConnection) databaseConnection).getSchema();
        if (schema == null || schema.isEmpty()) {
            return;
        }
        final List<String> searchPaths = getAllSearchPaths((JdbcConnection) databaseConnection);
        if (searchPaths == null/*in case of failure*/ || searchPaths.contains(schema)) {
            return;
        }
        searchPaths.add(schema);
        String dbName;
        try {
            dbName = databaseConnection.getCatalog();
        } catch (DatabaseException databaseException) {
            log.warning(("Couldn't get name from DatabaseConnection due to exception"), databaseException);
            return;
        }
        final String ALTER_SEARCH_PATH_QUERY = buildAlterSearchPathQuery(dbName, searchPaths);
        Statement statement = null;
        try {
            statement = ((JdbcConnection) databaseConnection).createStatement();
            statement.executeUpdate(ALTER_SEARCH_PATH_QUERY);
            log.info("Default schema: %s has been added to search paths successfully!");
        } catch (SQLException  sqlException) {
            log.severe(String.format("Schema:%s wasn't added to search path due to " +
                    "sqlException during execution of SQL statement:%s", schema, ALTER_SEARCH_PATH_QUERY), sqlException);
        } catch (DatabaseException databaseException) {
            log.severe(String.format("Schema:%s wasn't added to search path due to " +
                    "databaseException during execution of SQL statement:%s", schema, ALTER_SEARCH_PATH_QUERY), databaseException);
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

    private List<String> getAllSearchPaths(JdbcConnection databaseConnection) {
        final String SHOW_SEARCH_PATH_QUERY = "SHOW search_path";
        final List<String> searchPaths = new ArrayList<String>();
        Statement statement = null;
        ResultSet  resultSet = null;
        try  {
            statement = databaseConnection.createStatement();
             resultSet = statement.executeQuery(SHOW_SEARCH_PATH_QUERY);
            if (resultSet.next()) {
                String searchPath = resultSet.getString(1);
                List<String> schemas = Arrays.asList(searchPath.split(","));
                for (String schema: schemas) {
                    searchPaths.add(schema.trim());
                }
            }
        } catch (SQLException sqlException) {
            log.warning(String.format("Statement:%s couldn't be executed due to sqlException", SHOW_SEARCH_PATH_QUERY), sqlException);
            return null;
        } catch (DatabaseException databaseException) {
            log.warning(String.format("Statement:%s couldn't be executed due to databaseException", SHOW_SEARCH_PATH_QUERY), databaseException);
            return null;
        } finally {
            JdbcUtils.close(resultSet,statement);
        }
        return searchPaths;
    }

    private String buildAlterSearchPathQuery(String dbName, List<String> searchPaths) {
        return new StringBuilder().append(" ALTER DATABASE ").
                     append(dbName).
                     append(" SET ").
                     append(" search_path TO ").
                     append(StringUtils.join(searchPaths, ",")).toString();
    }

    /**
     * In this method we log.warn If edb_redwood_date is set to TRUE. Specific to PostgreSQL
     * @param databaseConnection - jdbc connection that will use in order to execute statement
     */
    private void logWarnIfStoreDateColumnNotSupported(final DatabaseConnection databaseConnection) {
        if (databaseConnection instanceof JdbcConnection) {
            Statement statement = null;
            ResultSet resultSet = null;
            final String SELECT_SETTING_QUERY = "select setting from pg_settings where name = 'edb_redwood_date'";
            try {
                statement =((JdbcConnection) databaseConnection).createStatement();
                resultSet = statement.executeQuery(SELECT_SETTING_QUERY);
                if (resultSet.next()) {
                    String setting = resultSet.getString(1);
                    if (setting != null && setting.equals("on")) {
                        log.warning("EnterpriseDB "+databaseConnection.getURL()+
                                " does not store DATE columns. Auto-converts them to TIMESTAMPs. (edb_redwood_date=true)");
                    }
                }
            } catch (Exception e) {
                log.info(String.format("Cannot check pg_settings due to exception during execution statement:%s", SELECT_SETTING_QUERY), e);
            } finally {
                JdbcUtils.close(resultSet,statement);
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

    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawCallStatement("select current_schema()");
    }

}