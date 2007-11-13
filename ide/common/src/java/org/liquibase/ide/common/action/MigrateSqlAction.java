package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

import java.io.StringWriter;

public class MigrateSqlAction extends MigratorAction {

    public MigrateSqlAction() {
        super("Generate Migration SQL");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        StringWriter stringWriter = new StringWriter();
        ideFacade.getMigrator(database).migrateSQL(stringWriter);

        ideFacade.displayOutput("Migration SQL", stringWriter.toString());
    }
}