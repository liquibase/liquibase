package liquibase.ext.bigquery.database;

import com.simba.googlebigquery.googlebigquery.core.BQDriver;
import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.configuration.ConfiguredValue;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class BigqueryDatabase extends AbstractJdbcDatabase {

    public static final String PRODUCT_NAME = BQDriver.DATABASE_NAME;
    public static final int BIGQUERY_PRIORITY_DATABASE = 510;
    private String liquibaseSchemaName;

    private static final Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", 34);

    public BigqueryDatabase() {
        this.setCurrentDateTimeFunction("CURRENT_DATETIME()");
        this.unquotedObjectsAreUppercased = false;
        this.addReservedWords(getDefaultReservedWords());
    }

    @Override
    public String getShortName() {
        return "bigquery";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    @Override
    public Integer getDefaultPort() {
        return 443;
    }

    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public String getCurrentDateTimeFunction() {
        return "CURRENT_DATETIME()";
    }

    @Override
    public String getDatabaseProductVersion() {
        return BQDriver.s_shortProductVersion;
    }

    @Override
    public int getDatabaseMajorVersion() {
        return BQDriver.DRIVER_MAJOR_VERSION;
    }

    @Override
    public int getDatabaseMinorVersion() {
        return BQDriver.DRIVER_MINOR_VERSION;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.trim().equalsIgnoreCase(conn.getDatabaseProductName().trim());
    }
    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectType.equals(Schema.class) || objectType.equals(Catalog.class)) {
            return objectName;
        }
        return super.escapeObjectName(objectName, objectType);
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:bigquery")) {
            return "com.simba.googlebigquery.jdbc.Driver";
        }

        return null;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public boolean supportsPrimaryKeyNames() {
        return false;
    }

    @Override
    public boolean supportsNotNullConstraintNames() {
        return false;
    }

    private String getDefaultDataset() {
        DatabaseConnection connection = getConnection();
        if (connection == null) {
            return null;
        }
        return BigqueryConnection.getUrlParamValue(connection.getURL(), "DefaultDataset");
    }

    @Override
    protected String getConnectionSchemaName() {
        return getDefaultDataset();
    }

    @Override
    public String getLiquibaseSchemaName() {
        if (this.liquibaseSchemaName != null) {
            return this.liquibaseSchemaName;
        } else {
            ConfiguredValue<String> configuredValue = GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentConfiguredValue();
            if (!configuredValue.wasDefaultValueUsed() && configuredValue.getValue() != null) {
                return configuredValue.getValue();
            } else {
                return this.getDefaultSchemaName();
            }
        }
    }

    @Override
    public void setLiquibaseSchemaName(String schemaName) {
        this.liquibaseSchemaName = schemaName;
    }

    @Override
    public String getJdbcCatalogName(final CatalogAndSchema schema) {
        DatabaseConnection connection = getConnection();
        try {
            return connection.getCatalog();
        } catch (DatabaseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        schema = schema.customize(this);
        String definition = (String) ((ExecutorService) Scope.getCurrentScope().getSingleton(ExecutorService.class))
                .getExecutor("jdbc", this)
                .queryForObject(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName), String.class);
        Scope.getCurrentScope().getLog(this.getClass()).info("getViewDefinition "+definition);
        return definition == null ? null : CREATE_VIEW_AS_PATTERN
                .matcher(definition)
                .replaceFirst("");
    }

    @Override
    public void setAutoCommit(final boolean b) {
        return;
    }

    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

    private Set<String> getDefaultReservedWords() {
        /*
         * List taken from
         * https://cloud.google.com/bigquery/docs/reference/standard-sql/lexical
         */

        Set<String> reservedWords = new HashSet<>();
        reservedWords.add("ALL");
        reservedWords.add("AND");
        reservedWords.add("ANY");
        reservedWords.add("ARRAY");
        reservedWords.add("AS");
        reservedWords.add("ASC");
        reservedWords.add("ASSERT_ROWS_MODIFIED");
        reservedWords.add("AT");
        reservedWords.add("BETWEEN");
        reservedWords.add("BY");
        reservedWords.add("CASE");
        reservedWords.add("CAST");
        reservedWords.add("COLLATE");
        reservedWords.add("CONTAINS");
        reservedWords.add("CREATE");
        reservedWords.add("CROSS");
        reservedWords.add("CUBE");
        reservedWords.add("CURRENT");
        reservedWords.add("DEFAULT");
        reservedWords.add("DEFINE");
        reservedWords.add("DESC");
        reservedWords.add("DISTINCT");
        reservedWords.add("ELSE");
        reservedWords.add("END");
        reservedWords.add("ENUM");
        reservedWords.add("ESCAPE");
        reservedWords.add("EXCEPT");
        reservedWords.add("EXCLUDE");
        reservedWords.add("EXISTS");
        reservedWords.add("EXTRACT");
        reservedWords.add("FALSE");
        reservedWords.add("FETCH");
        reservedWords.add("FOLLOWING");
        reservedWords.add("FOR");
        reservedWords.add("FROM");
        reservedWords.add("FULL");
        reservedWords.add("GROUP");
        reservedWords.add("GROUPING");
        reservedWords.add("GROUPS");
        reservedWords.add("HASH");
        reservedWords.add("HAVING");
        reservedWords.add("IF");
        reservedWords.add("IGNORE");
        reservedWords.add("IN");
        reservedWords.add("INNER");
        reservedWords.add("INTERSECT");
        reservedWords.add("INTERVAL");
        reservedWords.add("INTO");
        reservedWords.add("IS");
        reservedWords.add("JOIN");
        reservedWords.add("LATERAL");
        reservedWords.add("LEFT");
        reservedWords.add("LIKE");
        reservedWords.add("LIMIT");
        reservedWords.add("LOOKUP");
        reservedWords.add("MERGE");
        reservedWords.add("NATURAL");
        reservedWords.add("NEW");
        reservedWords.add("NO");
        reservedWords.add("NOT");
        reservedWords.add("NULL");
        reservedWords.add("NULLS");
        reservedWords.add("OF");
        reservedWords.add("ON");
        reservedWords.add("OR");
        reservedWords.add("ORDER");
        reservedWords.add("OUTER");
        reservedWords.add("OVER");
        reservedWords.add("PARTITION");
        reservedWords.add("PRECEDING");
        reservedWords.add("PROTO");
        reservedWords.add("RANGE");
        reservedWords.add("RECURSIVE");
        reservedWords.add("RESPECT");
        reservedWords.add("RIGHT");
        reservedWords.add("ROLLUP");
        reservedWords.add("ROWS");
        reservedWords.add("SELECT");
        reservedWords.add("SET");
        reservedWords.add("SOME");
        reservedWords.add("STRUCT");
        reservedWords.add("TABLESAMPLE");
        reservedWords.add("THEN");
        reservedWords.add("TO");
        reservedWords.add("TREAT");
        reservedWords.add("TRUE");
        reservedWords.add("UNBOUNDED");
        reservedWords.add("UNION");
        reservedWords.add("UNNEST");
        reservedWords.add("USING");
        reservedWords.add("WHEN");
        reservedWords.add("WHERE");
        reservedWords.add("WINDOW");
        reservedWords.add("WITH");
        reservedWords.add("WITHIN");

        return reservedWords;
    }
}
