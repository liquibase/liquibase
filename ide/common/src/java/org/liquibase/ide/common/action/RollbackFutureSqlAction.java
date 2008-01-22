package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

import java.io.StringWriter;

public class RollbackFutureSqlAction extends MigratorAction {

    public RollbackFutureSqlAction() {
        super("Generate SQL to Roll Back Unrun Changes");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        StringWriter writer = new StringWriter();

        String changeLogFile = ideFacade.promptForChangeLogFile();
        if (changeLogFile == null) {
            return;
        }

        ideFacade.getMigrator(changeLogFile, database).futureRollbackSQL(null, writer);
        ideFacade.showOutput("Rollback SQL", writer.toString());
    }

    public boolean needsRefresh() {
        return true;
    }
    
}
