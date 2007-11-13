package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

public class RollbackCountAction extends MigratorAction {

    public RollbackCountAction() {
        super("Rollback Changes");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        Integer input = ideFacade.promptForInteger(getTitle(), "Enter number of changes to rollback", 1);
        if (input != null) {
            ideFacade.getMigrator(null, database).rollbackCount(input);
        }
    }

    public boolean needsRefresh() {
        return true;
    }
    
}