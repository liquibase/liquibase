package liquibase.preconditions;

import liquibase.migrator.Migrator;
import liquibase.exception.PreconditionFailedException;
import liquibase.DatabaseChangeLog;

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


    public void check(Migrator migrator, DatabaseChangeLog changeLog) throws PreconditionFailedException {
        String dbType = migrator.getDatabase().getTypeName();
        if (!type.equals(dbType)) {
            throw new PreconditionFailedException(new FailedPrecondition("DBMS Precondition failed: expected "+dbType+", got "+type, changeLog, this));
        }
    }

    public String getTagName() {
        return "dbms";
    }

}
