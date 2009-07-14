package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DataType;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtils;

import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Encapsulates PostgreSQL database support.
 */
public class PostgresDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "PostgreSQL";

    private Set<String> systemTablesAndViews = new HashSet<String>();

    private String defaultDatabaseSchemaName;
    private static final DataType BOOLEAN_TYPE = new DataType("BOOLEAN", false);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL", true);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("TEXT", true);
    private static final DataType BLOB_TYPE = new DataType("BYTEA", false);
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP WITH TIME ZONE", false);

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

    public String getTypeName() {
        return "postgresql";
    }

    @Override
    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
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

    public DataType getBooleanType() {
        return BOOLEAN_TYPE;
    }

    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    public DataType getUUIDType() {
        return UUID_TYPE;
    }

    public DataType getClobType() {
        return CLOB_TYPE;
    }

    public DataType getBlobType() {
        return BLOB_TYPE;
    }

    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    public String getCurrentDateTimeFunction() {
        return "NOW()";
    }

    @Override
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {

        if (defaultDatabaseSchemaName == null) {
            try {
                List<String> searchPaths = getSearchPaths();
                if (searchPaths != null && searchPaths.size() > 0) {
                    for (String searchPath : searchPaths) {
                        if (searchPath != null && searchPath.length() > 0) {
                            defaultDatabaseSchemaName = searchPath;

                            if (defaultDatabaseSchemaName.equals("$user") && getConnection().getConnectionUserName() != null) {
                                if (!schemaExists(getConnection().getConnectionUserName())) {
                                    defaultDatabaseSchemaName = null;
                                } else {
                                    defaultDatabaseSchemaName = getConnection().getConnectionUserName();
                                }
                            }

                            if (defaultDatabaseSchemaName != null)
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: throw?
                e.printStackTrace();
                log.severe("Failed to get default catalog name from postgres", e);
            }
        }

        return defaultDatabaseSchemaName;
    }

    @Override
    public String getDefaultCatalogName() throws DatabaseException {
        return "public";
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
//            new WriteExecutor(this).execute(new RawSqlStatement("DROP OWNED BY " + schema));
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
    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        return super.isSystemTable(catalogName, schemaName, tableName)
                || "pg_catalog".equals(schemaName)
                || "pg_toast".equals(schemaName)
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
    public String getColumnType(String columnType, Boolean autoIncrement) {
        if (columnType.startsWith("java.sql.Types.VARCHAR")) { //returns "name" for type
            return columnType.replace("java.sql.Types.", "");
        }

        String type = super.getColumnType(columnType, autoIncrement);

        if (type.startsWith("TEXT(")) {
            return getClobType().getDataTypeName();
        } else if (type.toLowerCase().startsWith("float8")) {
            return "FLOAT8";
        } else if (type.toLowerCase().startsWith("float4")) {
            return "FLOAT4";
        }


        if (autoIncrement != null && autoIncrement) {
            if ("integer".equals(type.toLowerCase())) {
                return "serial";
            } else if ("bigint".equals(type.toLowerCase()) || "bigserial".equals(type.toLowerCase())) {
                return "bigserial";
            } else {
                // Unknown integer type, default to "serial"
                return "serial";
            }
        }

        return type;
    }


    @Override
    public String getAutoIncrementClause() {
        return "";
    }


    @Override
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits) throws ParseException {
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                defaultValue = ((String) defaultValue).replaceAll("'::[\\w\\s]+$", "'");

                if (dataType == Types.DATE || dataType == Types.TIME || dataType == Types.TIMESTAMP) {
                    //remove trailing time zone info
                    defaultValue = ((String) defaultValue).replaceFirst("-\\d+$", "");
                }
            }
        }
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits);

    }

    @Override
    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
        if (requestedSchema == null) {
            // Return the catalog name instead..
            return getDefaultCatalogName();
        } else {
            return StringUtils.trimToNull(requestedSchema).toLowerCase();
        }
    }

    @Override
    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
        return super.convertRequestedSchemaToCatalog(requestedSchema);
    }


    @Override
    public String escapeDatabaseObject(String objectName) {
        if (objectName == null) {
            return null;
        }
        if (hasCaseProblems(objectName) || isReservedWord(objectName)) {
            return "\"" + objectName + "\"";
        } else {
            return super.escapeDatabaseObject(objectName);
        }

    }

    /*
    * Check if given string has case problems according to postgresql documentation.
    * If there are at least one characters with upper case while all other are in lower case (or vice versa) this string should be escaped.
    */
    private boolean hasCaseProblems(String tableName) {
        return tableName.matches(".*[A-Z].*") && tableName.matches(".*[a-z].*");
    }

    /*
    * Check if given string is reserved word.
    */
    private boolean isReservedWord(String tableName) {
        for (int i = 0; i != this.reservedWords.length; i++)
            if (this.reservedWords[i].toLowerCase().equalsIgnoreCase(tableName))
                return true;
        return false;
    }

    /*
    * Reserved words from postgresql documentation
    */
    private String[] reservedWords = new String[]{"ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC", "AUTHORIZATION", "BETWEEN", "BINARY", "BOTH", "CASE", "CAST", "CHECK", "COLLATE", "COLUMN", "CONSTRAINT", "CORRESPONDING", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT", "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE", "END", "EXCEPT", "FALSE", "FOR", "FOREIGN", "FREEZE", "FROM", "FULL", "GRANT", "GROUP", "HAVING",
            "ILIKE", "IN", "INITIALLY", "INNER", "INTERSECT", "INTO", "IS", "ISNULL", "JOIN", "LEADING", "LEFT", "LIKE", "LIMIT", "LOCALTIME", "LOCALTIMESTAMP", "NATURAL", "NEW", "NOT", "NOTNULL", "NULL", "OFF", "OFFSET", "OLD", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUTER", "OVERLAPS", "PLACING", "PRIMARY", "REFERENCES", "RETURNING", "RIGHT", "SELECT", "SESSION_USER", "SIMILAR", "SOME", "SYMMETRIC", "TABLE", "THEN", "TO", "TRAILING", "TRUE", "UNION", "UNIQUE", "USER", "USING", "VERBOSE", "WHEN", "WHERE"};

    /*
     * Get the current search paths
     */
    private List<String> getSearchPaths() {
        List<String> searchPaths = null;

        try {
            DatabaseConnection con = getConnection();

            if (con != null) {
                String searchPathResult = (String) ExecutorService.getInstance().getReadExecutor(this).queryForObject(new RawSqlStatement("SHOW search_path"), String.class);

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
            log.severe("Failed to get default catalog name from postgres", e);
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
        Long count = ExecutorService.getInstance().getReadExecutor(this).queryForLong(new RawSqlStatement(query));

        return count != null && count > 0;
    }

    @Override
    protected Object convertToCorrectJavaType(String value, int dataType, int columnSize, int decimalDigits) throws ParseException {
        Object returnValue = super.convertToCorrectJavaType(value, dataType, columnSize, decimalDigits);
        if (returnValue != null && returnValue instanceof String) {
            if (((String) returnValue).startsWith("NULL::")) {
                return null;
            }
        }
        return returnValue;
    }
}
