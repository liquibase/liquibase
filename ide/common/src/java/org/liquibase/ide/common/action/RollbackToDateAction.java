package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

import java.util.Date;

public class RollbackToDateAction extends MigratorAction {

    public RollbackToDateAction() {
        super("Rollback to Date");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        Date input = ideFacade.promptForDateTime(getTitle(), "Enter date/time database tag to roll back to\nFormat: yyyy-dd-mm hh:mm:ss", new Date());
        if (input != null) {
            ideFacade.getMigrator(database).rollbackToDate(input);
        }
    }

}
