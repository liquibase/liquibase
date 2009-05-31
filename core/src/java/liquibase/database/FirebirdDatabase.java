package liquibase.database;

import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.FirebirdDatabaseSnapshot;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.SqlStatement;

import java.sql.Connection;
import java.util.Set;

/**
 * Firebird database implementation.
 * SQL Syntax ref: http://www.ibphoenix.com/main.nfs?a=ibphoenix&page=ibp_60_sqlref
 */
public class FirebirdDatabase extends AbstractDatabase {
    private static final DataType BOOLEAN_TYPE = new DataType("SMALLINT", false);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL(18, 4)", false);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("BLOB SUB_TYPE TEXT", false);
    private static final DataType BLOB_TYPE = new DataType("BLOB", false);
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP", false);

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


    @Override
    public boolean supportsSequences() {
        return true;
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

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        return "CURRENT_TIMESTAMP";
    }

    public boolean supportsTablespaces() {
        return false;
    }


    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }


    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public String getFalseBooleanValue() {
        return "0";
    }


    @Override
    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        return tableName.startsWith("RDB$") || super.isSystemTable(catalogName, schemaName, tableName);
    }


    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException {
        if (requestedSchema == null) {
            return getDefaultDatabaseSchemaName();
        } else {
            return requestedSchema.toUpperCase();
        }
    }

    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type = super.getColumnType(columnType, autoIncrement);
        if (type.startsWith("BLOB SUB_TYPE <0")) {
            return getBlobType().getDataTypeName();
        } else {
            return type;
        }
    }

    @Override
    public DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new FirebirdDatabaseSnapshot(this, statusListeners, schema);
    }
}
