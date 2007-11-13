package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;

public class ClearChecksumsAction extends MigratorAction {

    public ClearChecksumsAction() {
        super("Clear Checksums");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        ideFacade.getMigrator(database).clearCheckSums();

        ideFacade.displayMessage("Result", "Checksums cleared");

    }

}
