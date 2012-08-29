package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.statement.core.RawSqlStatement;

import java.math.BigInteger;
import java.util.*;

/**
 * Encapsulates PostgreSQL database support.
 */
public class PostgresDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "PostgreSQL";

    private Set<String> systemTablesAndViews = new HashSet<String>();

    private String defaultDatabaseSchemaName;

    private Set<String> reservedWords = new HashSet<String>();

    public PostgresDatabase() {
//        systemTablesAndViews.add("pg_logdir_ls");
//        systemTablesAndViews.add("administrable_role_authorizations");
//        systemTablesAndViews.add("applicable_roles");
//        systemTablesAndViews.add("attributes");
//        systemTablesAndViews.add("check_constraint_routine_usage");
//        systemTablesAndViews.add("check_constraints");
//        systemTablesAndViews.add("column_domain_usage");
//        systemTablesAndViews.add("column_privileges");
//        systemTablesAndViews.add("column_udt_usage");
//        systemTablesAndViews.add("columns");
//        systemTablesAndViews.add("constraint_column_usage");
//        systemTablesAndViews.add("constraint_table_usage");
//        systemTablesAndViews.add("data_type_privileges");
//        systemTablesAndViews.add("domain_constraints");
//        systemTablesAndViews.add("domain_udt_usage");
//        systemTablesAndViews.add("domains");
//        systemTablesAndViews.add("element_types");
//        systemTablesAndViews.add("enabled_roles");
//        systemTablesAndViews.add("key_column_usage");
//        systemTablesAndViews.add("parameters");
//        systemTablesAndViews.add("referential_constraints");
//        systemTablesAndViews.add("role_column_grants");
//        systemTablesAndViews.add("role_routine_grants");
//        systemTablesAndViews.add("role_table_grants");
//        systemTablesAndViews.add("role_usage_grants");
//        systemTablesAndViews.add("routine_privileges");
//        systemTablesAndViews.add("routines");
//        systemTablesAndViews.add("schemata");
//        systemTablesAndViews.add("sequences");
//        systemTablesAndViews.add("sql_features");
//        systemTablesAndViews.add("sql_implementation_info");
//        systemTablesAndViews.add("sql_languages");
//        systemTablesAndViews.add("sql_packages");
//        systemTablesAndViews.add("sql_parts");
//        systemTablesAndViews.add("sql_sizing");
//        systemTablesAndViews.add("sql_sizing_profiles");
//        systemTablesAndViews.add("table_constraints");
//        systemTablesAndViews.add("table_privileges");
//        systemTablesAndViews.add("tables");
//        systemTablesAndViews.add("triggers");
//        systemTablesAndViews.add("usage_privileges");
//        systemTablesAndViews.add("view_column_usage");
//        systemTablesAndViews.add("view_routine_usage");
//        systemTablesAndViews.add("view_table_usage");
//        systemTablesAndViews.add("views");
//        systemTablesAndViews.add("information_schema_catalog_name");
//        systemTablesAndViews.add("triggered_update_columns");
//        systemTablesAndViews.add("book_pkey");
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        try {
            reservedWords.addAll(Arrays.asList(((JdbcConnection) conn).getMetaData().getSQLKeywords().toUpperCase().split(",\\s*")));
            reservedWords.addAll(Arrays.asList("USER", "LIKE", "GROUP", "DATE", "ALL"));
        } catch (Exception e) {
            LogFactory.getLogger().warning("Cannot retrieve reserved words", e);
        }

        super.setConnection(conn);
    }

    public String getShortName() {
        return "postgresql";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "PostgreSQL";
    }

    public Integer getDefaultPort() {
        return 5432;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    protected String correctObjectName(String objectName) {
        return objectName.toLowerCase();
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }
        return null;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        
        return "NOW()";
    }

    @Override
    public String getDatabaseChangeLogTableName() {
        return super.getDatabaseChangeLogTableName().toLowerCase();
    }

    @Override
    public String getDatabaseChangeLogLockTableName() {
        return super.getDatabaseChangeLogLockTableName().toLowerCase();
    }

    

//    public void dropDatabaseObjects(String schema) throws DatabaseException {
//        try {
//            if (schema == null) {
//                schema = getConnectionUsername();
//            }
//            new Executor(this).execute(new RawSqlStatement("DROP OWNED BY " + schema));
//
//            getConnection().commit();
//
//            changeLogTableExists = false;
//            changeLogLockTableExists = false;
//            changeLogCreateAttempted = false;
//            changeLogLockCreateAttempted = false;
//
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
//    }


    @Override
    public boolean isSystemTable(Schema schema, String tableName) {
        schema = correctSchema(schema);
        return super.isSystemTable(schema, tableName)
                || "pg_catalog".equals(schema.getName())
                || "pg_toast".equals(schema.getName())
                || tableName.endsWith("_seq")
                || tableName.endsWith("_key")
                || tableName.endsWith("_pkey")
                || tableName.startsWith("idx_")
                || tableName.startsWith("pk_");
    }

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
    public String escapeDatabaseObject(String objectName) {
        if (objectName == null) {
            return null;
        }
        if (objectName.contains("-") || hasMixedCase(objectName) || startsWithNumeric(objectName) || isReservedWord(objectName)) {
            return "\"" + objectName + "\"";
        } else {
            return super.escapeDatabaseObject(objectName);
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
        return tableName.matches(".*[A-Z].*") && tableName.matches(".*[a-z].*");

    }

    /*
    * Check if given string starts with numeric values that may cause problems and should be escaped.
    */
    private boolean startsWithNumeric(String tableName) {
        return tableName.matches("^[0-9].*");

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
                String searchPathResult = (String) ExecutorService.getInstance().getExecutor(this).queryForObject(new RawSqlStatement("SHOW search_path"), String.class);

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
            LogFactory.getLogger().severe("Failed to get default catalog name from postgres", e);
        }

        return searchPaths;
    }


    private boolean catalogExists(String catalogName) throws DatabaseException {
        if (catalogName != null) {
            return runExistsQuery("select count(*) from information_schema.schemata where catalog_name='" + catalogName + "'");
        } else {
            return false;
        }
    }

    private boolean schemaExists(String schemaName) throws DatabaseException {
        return schemaName != null && runExistsQuery("select count(*) from information_schema.schemata where schema_name='" + schemaName + "'");
    }

    private boolean runExistsQuery(String query) throws DatabaseException {
        Long count = ExecutorService.getInstance().getExecutor(this).queryForLong(new RawSqlStatement(query));

        return count != null && count > 0;
    }

    @Override
    public String escapeIndexName(String catalogName,String schemaName, String indexName) {
        return escapeDatabaseObject(indexName);
    }
}
