package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.util.StringUtil;

public class Db2zDatabase extends AbstractDb2Database {

    public Db2zDatabase() {
        super.setCurrentDateTimeFunction("CURRENT TIMESTAMP");
        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
        super.sequenceCurrentValueFunction = "PREVIOUS VALUE FOR %s";
        super.unquotedObjectsAreUppercased=true;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("DB2") && StringUtil.startsWith(conn.getDatabaseProductVersion(), "DSN");
    }

    @Override
    public String getShortName() {
        return "db2z";
    }

    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        boolean isSystemIndex = example instanceof Index && example.getName() != null && example.getName().contains("_#_");
        boolean isSystemColumn = example instanceof Column && StringUtil.startsWith(example.getName(), "DB2_GENERATED");
        return isSystemIndex || isSystemColumn || super.isSystemObject(example);
    }

}
