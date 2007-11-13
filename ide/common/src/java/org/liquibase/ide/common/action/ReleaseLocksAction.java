package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;

public class ReleaseLocksAction extends MigratorAction {

    public ReleaseLocksAction() {
        super("Release LiquiBase Locks");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        ideFacade.getMigrator(database).releaseLock();

        ideFacade.displayMessage("Result", "All locks released successfully");

    }
}
