package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;

public class DropAllAction extends MigratorAction {

    public DropAllAction() {
        super("Drop All Objects");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {

        if (ideFacade.confirm(getTitle(), "Are you sure you want to drop all database objects?")) {
            ideFacade.getMigrator(null, database).dropAll();
            ideFacade.showMessage("Result", "All database objects dropped");
        }

    }

    public boolean needsRefresh() {
        return true;
    }

}
