package liquibase.database;

import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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

    protected String getBooleanType() {
        return "BOOLEAN";
    }

    protected String getCurrencyType() {
        return "DECIMAL";
    }

    protected String getUUIDType() {
        return null;
    }

    protected String getClobType() {
        return "TEXT";
    }

    protected String getBlobType() {
        return "BYTEA";
    }

    protected String getDateTimeType() {
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

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName;
    }

    public void dropDatabaseObjects() throws JDBCException, MigrationFailedException {
        Connection conn = getConnection();
        Statement dropStatement = null;
        try {
            dropStatement = conn.createStatement();
            dropStatement.executeUpdate("DROP OWNED BY " + getConnectionUsername());
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            try {
                if (dropStatement != null) {
                    dropStatement.close();
                }
                conn.commit();
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        }
    }


    public String createFindSequencesSQL() throws JDBCException {
        return "SELECT NULL AS SEQUENCE_SCHEMA, relname AS SEQUENCE_NAME FROM pg_class, pg_namespace WHERE relkind='S' AND pg_class.relnamespace = pg_namespace.oid AND nspname = '" + getSchemaName() + "'";
    }


    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        return super.isSystemTable(catalogName, schemaName, tableName)
                || "pg_catalog".equals(schemaName)
                || "pg_toast".equals(schemaName)
                || tableName.endsWith("_pkey")
                || tableName.startsWith("idx_")
                || tableName.startsWith("pk_");
    }

    @Override
    protected void dropSequences(Connection conn) throws JDBCException, MigrationFailedException {
    }

//    public boolean isSystemIndex(String catalogName, String schemaName, String indexName) {
//        return super.isSystemIndex(catalogName, schemaName, indexName)
//                || indexName.endsWith("_pkey");
//    }
}
