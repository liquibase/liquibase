package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

public class MigrateAction extends MigratorAction {

    public MigrateAction() {
        super("Update Database");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        ideFacade.getMigrator(database).migrate();
    }
}
