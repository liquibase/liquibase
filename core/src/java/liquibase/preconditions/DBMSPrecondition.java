package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.migrator.Migrator;

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
            throw new PreconditionFailedException("DBMS Precondition failed: expected "+type+", got "+dbType, changeLog, this);
        }
    }

    public String getTagName() {
        return "dbms";
    }

}
