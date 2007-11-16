package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.migrator.Migrator;

import java.sql.SQLException;

/**
 * Precondition that checks the name of the user executing the change log.
 */
public class RunningAsPrecondition implements Precondition {

    private String username;

    public RunningAsPrecondition() {
        username = "";
    }

    public void setUsername(String aUserName) {
        username = aUserName;
    }

    public String getUsername() {
        return username;
    }


    public void check(Migrator migrator, DatabaseChangeLog changeLog) throws PreconditionFailedException {
        try {
            String loggedusername = migrator.getDatabase().getConnection().getMetaData().getUserName();
            if (loggedusername.indexOf('@') >= 0) {
                loggedusername = loggedusername.substring(0, loggedusername.indexOf('@'));
            }
            if (!username.equals(loggedusername)) {
                throw new PreconditionFailedException("RunningAs Precondition failed: expected "+username+", was "+loggedusername, changeLog, this);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot determine username", e);
        }
    }

    public String getTagName() {
        return "runningAs";
    }

}
