package liquibase.database;

import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.PostgresDatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.exception.CustomChangeException;
import liquibase.util.StringUtils;
import liquibase.diff.DiffStatusListener;

import java.sql.*;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Encapsulates PostgreSQL database support.
 */
public class PostgresDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "PostgreSQL";

    private Set<String> systemTablesAndViews = new HashSet<String>();

    private String defaultDatabaseSchemaName;
    private String defaultCatalogName;

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

    public String getProductName() {
        return "PostgreSQL";
    }

    public String getTypeName() {
        return "postgresql";
    }

    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }
        return null;
    }

    public String getBooleanType() {
        return "BOOLEAN";
    }

    public String getCurrencyType() {
        return "DECIMAL";
    }

    public String getUUIDType() {
        return "CHAR(36)";
    }

    public String getClobType() {
        return "TEXT";
    }

    public String getBlobType() {
        return "BYTEA";
    }

    public String getDateTimeType() {
        return "TIMESTAMP WITH TIME ZONE";
    }

    public boolean supportsSequences() {
        return true;
    }

    public String getCurrentDateTimeFunction() {
        return "NOW()";
    }

    protected String getDefaultDatabaseSchemaName() throws JDBCException {

        if (defaultDatabaseSchemaName == null) {
            try {
                List<String> searchPaths = getSearchPaths();
                if (searchPaths != null && searchPaths.size() > 0) {
                    for (String searchPath : searchPaths) {
                        if (searchPath != null && searchPath.length() > 0) {
                            defaultDatabaseSchemaName = searchPath;

                            if (defaultDatabaseSchemaName.equals("$user") && getConnectionUsername() != null) {
                                if (! schemaExists(getConnectionUsername())) {
                                    defaultDatabaseSchemaName = null;
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
                log.log(Level.SEVERE, "Failed to get default catalog name from postgres", e);
            }
        }

        return defaultDatabaseSchemaName;
    }

    public String getDefaultCatalogName() throws JDBCException {

        if (defaultCatalogName == null) {
            try {
                List<String> searchPaths = getSearchPaths();
                if (searchPaths != null && searchPaths.size() > 0) {
                    for (String searchPath : searchPaths) {
                        if (searchPath != null && searchPath.length() > 0) {
                            defaultCatalogName = searchPath;

                            if (defaultCatalogName.equals("$user") && getConnectionUsername() != null) {
                                if (! catalogExists(getConnectionUsername())) {
                                    defaultCatalogName = null;
                                } else {
                                    defaultCatalogName = getConnectionUsername();
                                }
                            }

                            if (defaultCatalogName != null)
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: throw?
                e.printStackTrace();
                log.log(Level.SEVERE, "Failed to get default catalog name from postgres", e);
            }

            // Default
            if (defaultCatalogName == null) {
                defaultCatalogName = "PUBLIC";
            }

        }

        return defaultCatalogName;
    }

    public String getDatabaseChangeLogTableName() {
        return super.getDatabaseChangeLogTableName().toLowerCase();
    }

    public String getDatabaseChangeLogLockTableName() {
        return super.getDatabaseChangeLogLockTableName().toLowerCase();
    }

//    public void dropDatabaseObjects(String schema) throws JDBCException {
//        try {
//            if (schema == null) {
//                schema = getConnectionUsername();
//            }
//            new JdbcTemplate(this).execute(new RawSqlStatement("DROP OWNED BY " + schema));
//
//            getConnection().commit();
//
//            changeLogTableExists = false;
//            changeLogLockTableExists = false;
//            changeLogCreateAttempted = false;
//            changeLogLockCreateAttempted = false;
//
//        } catch (SQLException e) {
//            throw new JDBCException(e);
//        }
//    }


    public SqlStatement createFindSequencesSQL(String schema) throws JDBCException {
        return new RawSqlStatement("SELECT relname AS SEQUENCE_NAME FROM pg_class, pg_namespace WHERE relkind='S' AND pg_class.relnamespace = pg_namespace.oid AND nspname = '" + convertRequestedSchemaToSchema(schema) + "' AND 'nextval(''" + (schema == null ? "" : schema + ".") + "'||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) AND 'nextval('''||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null)");
    }


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


    public SqlStatement getViewDefinitionSql(String schemaName, String name) throws JDBCException {
        return new RawSqlStatement("select definition from pg_views where viewname='" + name + "' AND schemaname='" + convertRequestedSchemaToSchema(schemaName) + "'");
    }


    public String getColumnType(String columnType, Boolean autoIncrement) {
        if (columnType.startsWith("java.sql.Types.VARCHAR")) { //returns "name" for type
            return columnType.replace("java.sql.Types.", "");
        }

        String type = super.getColumnType(columnType, autoIncrement);

        if (type.startsWith("TEXT(")) {
            return getClobType();
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


    public String getAutoIncrementClause() {
        return "";
    }


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

    public String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException {
        if (requestedSchema == null) {
            // Return the catalog name instead..
            return getDefaultCatalogName();
        } else {
            return StringUtils.trimToNull(requestedSchema).toLowerCase();
        }
    }

    public String convertRequestedSchemaToCatalog(String requestedSchema) throws JDBCException {
        return super.convertRequestedSchemaToCatalog(requestedSchema);
    }

    /**
     * @see liquibase.database.AbstractDatabase#escapeTableName(java.lang.String, java.lang.String)
     */
    @Override
    public String escapeTableName(String schemaName, String tableName) {
        //Check if tableName is in reserved words and has CaseSensitivity problems
        if (StringUtils.trimToNull(tableName) != null && (hasCaseProblems(tableName) || isReservedWord(tableName))) {
            return super.escapeTableName(schemaName, "\"" + tableName + "\"");
        }
        return super.escapeTableName(schemaName, tableName);
    }

    /**
     * @see liquibase.database.AbstractDatabase#escapeColumnName(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String escapeColumnName(String schemaName, String tableName, String columnName) {
        if (hasCaseProblems(columnName) || isReservedWord(columnName))
            return "\"" + columnName + "\"";
        return columnName;
    }

    /*
    * Check if given string has case problems according to postgresql documentation.
    * If there are at least one characters with upper case while all other are in lower case (or vice versa) this string should be escaped.
    */
    private boolean hasCaseProblems(String tableName) {
        if (tableName.matches(".*[A-Z].*") && tableName.matches(".*[a-z].*"))
            return true;
        return false;
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
                Statement stmt = con.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);

                ResultSet searchPathQry = stmt.executeQuery("SHOW search_path");

                if (searchPathQry.next()) {
                    String searchPathResult = searchPathQry.getString(1);
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

            }
        } catch (Exception e) {
            // TODO: Something?
            e.printStackTrace();
            log.log(Level.SEVERE, "Failed to get default catalog name from postgres", e);
        }

        return searchPaths;
    }


    private boolean catalogExists(String catalogName) throws SQLException {
        if (catalogName != null) {
            return runExistsQuery("select count(*) from information_schema.schemata where catalog_name='" + catalogName + "'");
        } else {
            return false;
        }
    }

    private boolean schemaExists(String schemaName) throws SQLException {
        if (schemaName != null) {
            return runExistsQuery("select count(*) from information_schema.schemata where schema_name='" + schemaName + "'");
        } else {
            return false;
        }
    }

    private boolean runExistsQuery(String query) throws SQLException {
        DatabaseConnection con = getConnection();

        Statement stmt = con.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);

        ResultSet existsQry = stmt.executeQuery(query);

        if (existsQry.next()) {
            Integer count = existsQry.getInt(1);

            if (count != null && count > 0) {
                return true;
            }
        }

        return false;
    }

    public DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new PostgresDatabaseSnapshot(this, statusListeners, schema);
    }
}
