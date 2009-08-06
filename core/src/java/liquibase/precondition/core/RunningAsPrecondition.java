package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.Precondition;

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


    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        String loggedusername = database.getConnection().getConnectionUserName();
        if (loggedusername.indexOf('@') >= 0) {
            loggedusername = loggedusername.substring(0, loggedusername.indexOf('@'));
        }
        if (!username.equalsIgnoreCase(loggedusername)) {
            throw new PreconditionFailedException("RunningAs Precondition failed: expected "+username+", was "+loggedusername, changeLog, this);
        }
    }

    public String getName() {
        return "runningAs";
    }

}
