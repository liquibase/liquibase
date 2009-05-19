package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

public class RollbackCountAction extends MigratorAction {

    public RollbackCountAction() {
        super("Rollback Changes");
    }

    @Override
    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        Integer input = ideFacade.promptForInteger(getTitle(), "Enter number of changes to rollback", 1);
        if (input != null) {
            ideFacade.getLiquibase(null, database).rollback(input, null);
        }
    }

    @Override
    public boolean needsRefresh() {
        return true;
    }
    
}