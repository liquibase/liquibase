package org.liquibase.ide.common.action;

import liquibase.exception.LiquibaseException;
import liquibase.database.Database;
import org.liquibase.ide.common.IdeFacade;

import java.io.StringWriter;
import java.util.Date;

public class RollbackToDateSqlAction extends MigratorAction {

    public RollbackToDateSqlAction() {
        super("Rollback to Date (Generate SQL)");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        Date input = ideFacade.promptForDateTime(getTitle(), "Enter date/time database tag to roll back to\nFormat: yyyy-dd-mm hh:mm:ss", new Date());
        if (input != null) {
            StringWriter writer = new StringWriter();
            ideFacade.getMigrator(null, database).rollbackToDateSQL(input, writer);

            ideFacade.showOutput("Rollback SQL", writer.toString());
        }
    }

    public boolean needsRefresh() {
        return true;
    }
    

}