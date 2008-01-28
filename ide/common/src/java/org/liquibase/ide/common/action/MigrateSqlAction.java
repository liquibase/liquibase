package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

import java.io.StringWriter;

public class MigrateSqlAction extends MigratorAction {

    public MigrateSqlAction() {
        super("Generate Migration SQL");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        StringWriter stringWriter = new StringWriter();

        String changeLogFile = ideFacade.promptForChangeLogFile();
        if (changeLogFile == null) {
            return;
        }

        ideFacade.getLiquibase(changeLogFile, database).update(null, stringWriter);

        ideFacade.showOutput("Migration SQL", stringWriter.toString());
    }

    public boolean needsRefresh() {
        return true;
    }
    
}