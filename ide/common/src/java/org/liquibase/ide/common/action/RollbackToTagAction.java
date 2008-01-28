package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

public class RollbackToTagAction extends MigratorAction {

    public RollbackToTagAction() {
        super("Rollback to Tag");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        String input = ideFacade.promptForString(getTitle(), "Enter database tag to roll back to", null);
        if (input != null) {
            ideFacade.getLiquibase(null, database).rollback(input, null);
        }
    }

    public boolean needsRefresh() {
        return true;
    }


}
