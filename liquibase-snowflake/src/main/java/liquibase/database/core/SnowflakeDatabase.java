package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.StoredProcedure;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.SystemUtil;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SnowflakeDatabase extends AbstractJdbcDatabase {

    public static final String PRODUCT_NAME = "Snowflake";
    private static final Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private final Set<String> systemTables = new HashSet<>();
    private final Set<String> systemViews = new HashSet<>();

    public SnowflakeDatabase() {
        super.setCurrentDateTimeFunction("current_timestamp::timestamp_ntz");
        super.unmodifiableDataTypes.addAll(Arrays.asList("integer", "bool", "boolean", "int4", "int8", "float4", "float8", "numeric",
            "bigserial", "serial", "bytea", "timestamptz", "array", "object", "variant"));
        super.unquotedObjectsAreUppercased = true;
        super.addReservedWords(getDefaultReservedWords());
        super.defaultAutoIncrementStartWith = BigInteger.ONE;
        super.defaultAutoIncrementBy = BigInteger.ONE;
        super.sequenceNextValueFunction = "%s.nextval";
    }

    @Override
    public String getShortName() {
        return "snowflake";
    }

    @Override
    public String getDisplayName() {
        return "Snowflake";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    @Override
    public Integer getDefaultPort() {
        return null;
    }

    @Override
    public Set<String> getSystemTables() {
        return systemTables;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemViews;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        // Snowflake doesn't enforce constraints, so statement won't throw error, but won't drop table as well
        // https://docs.snowflake.com/en/sql-reference/constraints.html
        return true;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:snowflake:")) {
            return "net.snowflake.client.jdbc.SnowflakeDriver";
        }
        return null;
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return type == Table.class || type == View.class || type == StoredProcedure.class;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return true;
    }

    @Override
    public String getAutoIncrementClause() {
        return "AUTOINCREMENT";
    }

    @Override
    protected String getAutoIncrementStartWithClause() {
        return "%d";
    }

    @Override
    protected String getAutoIncrementByClause() {
        return "%d";
    }

    @Override
    public boolean generateAutoIncrementStartWith(BigInteger startWith) {
        return true;
    }

    @Override
    public boolean generateAutoIncrementBy(BigInteger incrementBy) {
        return true;
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    @Override
    protected String getConnectionSchemaName() {
        DatabaseConnection connection = getConnection();
        if (connection == null) {
            return null;
        }
        try (ResultSet resultSet = ((JdbcConnection) connection).createStatement().executeQuery("SELECT CURRENT_SCHEMA()")) {
            resultSet.next();
            return resultSet.getString(1);
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).info("Error getting default schema", e);
        }
        return null;
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);

        configureSession();
    }

    @Override
    public void rollback() throws DatabaseException {
        super.rollback();
        configureSession();
    }

    protected void configureSession() {
        //Due to the snowflake driver's use of reflection on java.nio within the arrow support, queries fail with a "can't load class" error if the default arrow result format is returned
        //This ensures we use the json format which does work with 17+
        //We don't force it if we are running under java 17 since arrow does work for those versions
        if (SystemUtil.getJavaMajorVersion() >= 17) {
            try {
                Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).execute(new RawParameterizedSqlStatement("alter session set jdbc_query_result_format = 'JSON'"));
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
            }
        }
    }

    private Set<String> getDefaultReservedWords() {
        /*
         * List taken from
         * https://docs.snowflake.net/manuals/sql-reference/reserved-keywords.html
         */

        Set<String> reservedWords = new HashSet<>();
        reservedWords.add("ACCOUNT");
        reservedWords.add("ALL");
        reservedWords.add("ALTER");
        reservedWords.add("AND");
        reservedWords.add("ANY");
        reservedWords.add("AS");
        reservedWords.add("BETWEEN");
        reservedWords.add("BY");
        reservedWords.add("CASE");
        reservedWords.add("CAST");
        reservedWords.add("CHECK");
        reservedWords.add("COLUMN");
        reservedWords.add("CONNECT");
        reservedWords.add("CONNECTION");
        reservedWords.add("CONSTRAINT");
        reservedWords.add("CREATE");
        reservedWords.add("CROSS");
        reservedWords.add("CURRENT");
        reservedWords.add("CURRENT_TIME");
        reservedWords.add("CURRENT_TIMESTAMP");
        reservedWords.add("CURRENT_USER");
        reservedWords.add("DATABASE");
        reservedWords.add("DELETE");
        reservedWords.add("DISTINCT");
        reservedWords.add("DROP");
        reservedWords.add("ELSE");
        reservedWords.add("EXISTS");
        reservedWords.add("FALSE");
        reservedWords.add("FOLLOWING");
        reservedWords.add("FOR");
        reservedWords.add("FROM");
        reservedWords.add("FULL");
        reservedWords.add("GRANT");
        reservedWords.add("GROUP");
        reservedWords.add("GSCLUSTER");
        reservedWords.add("HAVING");
        reservedWords.add("ILIKE");
        reservedWords.add("IN");
        reservedWords.add("INCREMENT");
        reservedWords.add("INNER");
        reservedWords.add("INSERT");
        reservedWords.add("INTERSECT");
        reservedWords.add("INTO");
        reservedWords.add("IS");
        reservedWords.add("ISSUE");
        reservedWords.add("JOIN");
        reservedWords.add("LATERAL");
        reservedWords.add("LEFT");
        reservedWords.add("LIKE");
        reservedWords.add("LOCALTIME");
        reservedWords.add("LOCALTIMESTAMP");
        reservedWords.add("MINUS");
        reservedWords.add("NATURAL");
        reservedWords.add("NOT");
        reservedWords.add("NULL");
        reservedWords.add("OF");
        reservedWords.add("ON");
        reservedWords.add("OR");
        reservedWords.add("ORDER");
        reservedWords.add("ORGANIZATION");
        reservedWords.add("QUALIFY");
        reservedWords.add("REGEXP");
        reservedWords.add("REVOKE");
        reservedWords.add("RIGHT");
        reservedWords.add("RLIKE");
        reservedWords.add("ROW");
        reservedWords.add("ROWS");
        reservedWords.add("SAMPLE");
        reservedWords.add("SCHEMA");
        reservedWords.add("SELECT");
        reservedWords.add("SET");
        reservedWords.add("SOME");
        reservedWords.add("START");
        reservedWords.add("TABLE");
        reservedWords.add("TABLESAMPLE");
        reservedWords.add("THEN");
        reservedWords.add("TO");
        reservedWords.add("TRIGGER");
        reservedWords.add("TRUE");
        reservedWords.add("TRY_CAST");
        reservedWords.add("UNION");
        reservedWords.add("UNIQUE");
        reservedWords.add("UPDATE");
        reservedWords.add("USING");
        reservedWords.add("VALUES");
        reservedWords.add("VIEW");
        reservedWords.add("WHEN");
        reservedWords.add("WHENEVER");
        reservedWords.add("WHERE");
        reservedWords.add("WITH");

        return reservedWords;
    }

    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        String definition = super.getViewDefinition(schema, viewName);
        if (definition == null || definition.isEmpty()) {
            Scope.getCurrentScope()
                .getLog(getClass())
                .info("Error reading '" + (viewName != null && viewName.isEmpty() ? viewName : "") + "' view definition");
            return null;
        }
        if (definition.endsWith(";")) {
            definition = definition.substring(0, definition.length() - 1);
        }
        return CREATE_VIEW_AS_PATTERN.matcher(definition).replaceFirst("");
    }

    @Override
    public boolean supportsDatabaseChangeLogHistory() {
        return true;
    }

    @Override
    public String generateConnectCommandSuccessMessage() {
        return "WARNING: The 'connect' command relies on information reported by the JDBC driver. " +
                "The Snowflake driver does not report on schema issues, " +
                "and therefore users should manually confirm Snowflake schemas for accuracy.";
    }
}
