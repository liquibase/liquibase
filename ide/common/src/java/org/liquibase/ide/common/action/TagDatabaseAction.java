package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

public class TagDatabaseAction extends MigratorAction {

    public TagDatabaseAction() {
        super("Tag Database");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        String input = ideFacade.promptForString(getTitle(), "Enter database tag", null);
        if (input != null) {
            ideFacade.getMigrator(null, database).tag(input);
        }
    }

    public boolean needsRefresh() {
        return false;
    }

}