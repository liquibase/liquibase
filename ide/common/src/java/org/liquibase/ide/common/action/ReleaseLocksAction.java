package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.lock.LockService;
import org.liquibase.ide.common.IdeFacade;

public class ReleaseLocksAction extends MigratorAction {

    public ReleaseLocksAction() {
        super("Release LiquiBase Locks");
    }

    @Override
    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        LockService.getInstance(database).releaseLock();

        ideFacade.showMessage("Result", "All locks released successfully");

    }

    @Override
    public boolean needsRefresh() {
        return false;
    }
}
