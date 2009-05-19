package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

public class MigrateAction extends MigratorAction {

    public MigrateAction() {
        super("Update Database");
    }

    @Override
    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        String changeLogFile = ideFacade.promptForChangeLogFile();
        if (changeLogFile == null) {
            return;
        }
        ideFacade.getLiquibase(changeLogFile, database).update(null);
    }

    @Override
    public boolean needsRefresh() {
        return true;
    }

}
