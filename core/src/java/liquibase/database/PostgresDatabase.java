package liquibase.database;

import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.util.StringUtils;

import java.sql.Connection;
import java.sql.Types;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates PostgreSQL database support.
 */
public class PostgresDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "PostgreSQL";

    private Set<String> systemTablesAndViews = new HashSet<String>();

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

    public String getSchemaName() throws JDBCException {
        return null;
    }

    public String getCatalogName() throws JDBCException {
        return "PUBLIC";
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
        return new RawSqlStatement("SELECT relname AS SEQUENCE_NAME FROM pg_class, pg_namespace WHERE relkind='S' AND pg_class.relnamespace = pg_namespace.oid AND nspname = '" + convertRequestedSchemaToSchema(schema) + "'");
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
        return new RawSqlStatement("select definition from pg_views where viewname='" + name + "' AND schemaname='"+convertRequestedSchemaToSchema(schemaName)+"'");
    }


    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type = super.getColumnType(columnType, autoIncrement);

        if (autoIncrement != null && autoIncrement) {
            if ("integer".equals(type.toLowerCase())) {
                return "serial";
            } else if ("bigint".equals(type.toLowerCase())) {
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
            return "public";
        } else {
            return StringUtils.trimToNull(requestedSchema).toLowerCase();
        }
    }

    public String convertRequestedSchemaToCatalog(String requestedSchema) throws JDBCException {
        return null;
    }
}
