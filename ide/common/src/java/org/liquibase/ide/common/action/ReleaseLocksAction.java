package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.lock.LockHandler;
import org.liquibase.ide.common.IdeFacade;

public class ReleaseLocksAction extends MigratorAction {

    public ReleaseLocksAction() {
        super("Release LiquiBase Locks");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        LockHandler.getInstance(database).releaseLock();

        ideFacade.showMessage("Result", "All locks released successfully");

    }

    public boolean needsRefresh() {
        return false;
    }
}
