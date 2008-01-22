package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionFailedException;

/**
 * Precondition for specifying the type of database (oracle, mysql, etc.).
 */

public class DBMSPrecondition implements Precondition {
    private String type;


    public DBMSPrecondition() {
    }

    public String getType() {
        return type;
    }

    public void setType(String atype) {
        this.type = atype.toLowerCase();
    }


    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException {
        String dbType = database.getTypeName();
        if (!type.equals(dbType)) {
            throw new PreconditionFailedException("DBMS Precondition failed: expected "+type+", got "+dbType, changeLog, this);
        }
    }

    public String getTagName() {
        return "dbms";
    }

}
