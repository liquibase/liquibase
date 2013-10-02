package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.Precondition;

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


    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        try {
            String dbType = database.getShortName();
            if (!DatabaseList.definitionMatches(this.type, database, false)) {
                throw new PreconditionFailedException("DBMS Precondition failed: expected "+type+", got "+dbType, changeLog, this);
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "dbms";
    }

}
