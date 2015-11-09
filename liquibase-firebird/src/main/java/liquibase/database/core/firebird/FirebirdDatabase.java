package liquibase.database.core.firebird;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.structure.DatabaseObject;
import liquibase.exception.DatabaseException;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Table;

/**
 * Firebird database implementation.
 * SQL Syntax ref: http://www.ibphoenix.com/main.nfs?a=ibphoenix&page=ibp_60_sqlref
 */
public class FirebirdDatabase extends AbstractJdbcDatabase {

    public FirebirdDatabase() {
        super.setCurrentDateTimeFunction("CURRENT_TIMESTAMP");
        super.sequenceNextValueFunction="NEXT VALUE FOR %s";
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("Firebird");
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:firebirdsql")) {
            return "org.firebirdsql.jdbc.FBDriver";
        }
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    @Override
    public String getShortName() {
        return "firebird";
    }

    @Override
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

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }


    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

    @Override
    public boolean isSystemObject(ObjectReference example) {
        if (example.instanceOf(Table.class) && example.name.startsWith("RDB$")) {
            return true;
        }
        return super.isSystemObject(example);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }

    @Override
    public int getMaxReferenceContainerDepth() {
        return 0;
    }

    @Override
    public int getMaxSnapshotContainerDepth() {
        return 0;
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }
        return objectName.toUpperCase().trim();
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return true;
    }
}
