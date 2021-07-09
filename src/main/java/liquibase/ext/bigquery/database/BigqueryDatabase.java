package liquibase.ext.bigquery.database;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.configuration.ConfiguredValue;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;

public class BigqueryDatabase extends AbstractJdbcDatabase {

    public static final String PRODUCT_NAME = "Google BigQuery";
    private DatabaseConnection connection;
    private String liquibaseSchemaName;

    public BigqueryDatabase() {
        super.setCurrentDateTimeFunction("current_timestamp::timestamp_ntz");
        super.unmodifiableDataTypes.addAll(Arrays.asList("integer", "bool", "boolean", "int4", "int8", "float4", "float8", "numeric", "bigserial", "serial", "bytea", "timestamptz"));
        super.unquotedObjectsAreUppercased = false;
        super.addReservedWords(getDefaultReservedWords());
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
        return PRIORITY_DATABASE;
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
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
            return "com.simba.googlebigquery.jdbc.Driver";
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    private String getDefaultDataset(){
        DatabaseConnection connection = getConnection();
        if (connection == null) {
            return null;
        }
        // read dataset, DefaultDataset
        String[] uriArgs = connection.getURL().replace(" ","").split(";");
        Optional<String> defaultDatasetStr = Arrays.stream(uriArgs)
            .filter(x -> x.startsWith("DefaultDataset="))
            .findFirst();

        if(!defaultDatasetStr.isPresent()) {
            return null;
        }
        String[] defaultDatasetArr = defaultDatasetStr.get().split("=");
        return defaultDatasetArr[1];
    }

    @Override
    protected String getConnectionSchemaName() {
        return getDefaultDataset();
    }


    @Override
    public String getDefaultSchemaName() {
        if (!supportsSchemas()) {
            return getDefaultCatalogName();
        }

        if (defaultSchemaName == null) {
            defaultSchemaName = getConnectionSchemaName();
            Scope.getCurrentScope().getLog(getClass()).info("Set default schema name to " + defaultSchemaName);
        }

        return defaultSchemaName;
    }

    @Override
    public String getLiquibaseSchemaName() {
        if (liquibaseSchemaName != null) {
            return liquibaseSchemaName;
        }

        final ConfiguredValue<String> configuredValue = GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentConfiguredValue();
        if (!configuredValue.wasDefaultValueUsed()) {
            return configuredValue.getValue();
        }

        return getDefaultSchemaName();
    }

    @Override
    public boolean supportsSchemas() {
        return true;
    }


    @Override
    public void setAutoCommit(final boolean b) throws DatabaseException {
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
