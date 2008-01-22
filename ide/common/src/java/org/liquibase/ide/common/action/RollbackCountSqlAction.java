package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

import java.io.StringWriter;

public class RollbackCountSqlAction extends MigratorAction {

    public RollbackCountSqlAction() {
        super("Rollback Changes (Generate SQL)");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        Integer input = ideFacade.promptForInteger(getTitle(), "Enter number of changes to rollback", 1);
        if (input != null) {
            StringWriter writer = new StringWriter();
            ideFacade.getMigrator(null, database).rollback(input, null, writer);

            ideFacade.showOutput("Rollback SQL", writer.toString());
        }
    }

    public boolean needsRefresh() {
        return true;
    }
    
}