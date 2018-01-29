package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.AbstractPrecondition;

/**
 * Precondition that checks the name of the user executing the change log.
 */
public class RunningAsPrecondition extends AbstractPrecondition {

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

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        String loggedusername = database.getConnection().getConnectionUserName();
        if ((loggedusername != null) && (loggedusername.indexOf('@') >= 0)) {
            loggedusername = loggedusername.substring(0, loggedusername.indexOf('@'));
        }
        if (!username.equalsIgnoreCase(loggedusername)) {
            throw new PreconditionFailedException("RunningAs Precondition failed: expected "+username+", was "+loggedusername, changeLog, this);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "runningAs";
    }

}
