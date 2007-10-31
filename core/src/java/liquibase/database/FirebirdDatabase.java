package liquibase.database;

import liquibase.exception.JDBCException;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.RawSqlStatement;

import java.sql.Connection;

/**
 * Firebird database implementation.
 * SQL Syntax ref: http://www.ibphoenix.com/main.nfs?a=ibphoenix&page=ibp_60_sqlref
 */
public class FirebirdDatabase extends AbstractDatabase {

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return getDatabaseProductName(conn).startsWith("Firebird");
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:firebirdsql")) {
            return "org.firebirdsql.jdbc.FBDriver";
        }
        return null;
    }

    public String getProductName() {
        return "Firebird";
    }

    public String getTypeName() {
        return "firebird";
    }


    public boolean supportsSequences() {
        return true;
    }

    public String getBooleanType() {
        return "SMALLINT";
    }

    public String getCurrencyType() {
        return "DECIMAL(18, 4)";
    }

    public String getUUIDType() {
        return "CHAR(36)";
    }

    public String getClobType() {
        return "BLOB SUB_TYPE TEXT";
    }

    public String getBlobType() {
        return "BLOB";
    }

    public String getDateTimeType() {
        return "TIMESTAMP";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        return "CURRENT_TIMESTAMP";
    }

    public boolean supportsTablespaces() {
        return false;
    }


    public SqlStatement createFindSequencesSQL(String schema) throws JDBCException {
        return new RawSqlStatement("SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS WHERE RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0");
    }

    public SqlStatement getViewDefinitionSql(String schemaName, String viewName) throws JDBCException {
        String sql = "select rdb$view_source from rdb$relations where upper(rdb$relation_name)='" + viewName + "'";
//        if (schemaName != null) {
//            sql += " and rdb$owner_name='" + schemaName.toUpperCase() + "'";
//        }
//        if (getCatalogName() != null) {
//            sql += " and table_catalog='" + getCatalogName() + "'";
//
//        }
        return new RawSqlStatement(sql);
    }


    public boolean supportsDDLInTransaction() {
        return false;
    }


    public String getTrueBooleanValue() {
        return "1";
    }

    public String getFalseBooleanValue() {
        return "0";
    }


    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        if (tableName.startsWith("RDB$")) {
            return true;
        }
        return super.isSystemTable(catalogName, schemaName, tableName);
    }


    public boolean supportsAutoIncrement() {
        return false;
    }

    public boolean supportsSchemas() {
        return false;
    }

    public String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException {
        if (requestedSchema == null) {
            return getSchemaName();
        } else {
            return requestedSchema.toUpperCase();
        }
    }
}
