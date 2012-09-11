package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.exception.DatabaseException;

/**
 * Firebird database implementation.
 * SQL Syntax ref: http://www.ibphoenix.com/main.nfs?a=ibphoenix&page=ibp_60_sqlref
 */
public class FirebirdDatabase extends AbstractDatabase {

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("Firebird");
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:firebirdsql")) {
            return "org.firebirdsql.jdbc.FBDriver";
        }
        return null;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    public String getShortName() {
        return "firebird";
    }

    public Integer getDefaultPort() {
        return 3050;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Firebird";
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }

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
    public boolean isSystemTable(Schema schema, String tableName) {
        return tableName.startsWith("RDB$") || super.isSystemTable(schema, tableName);
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
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return objectName.toUpperCase();
    }
}
