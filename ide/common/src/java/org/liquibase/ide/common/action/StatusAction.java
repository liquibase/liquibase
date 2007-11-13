package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StatusAction extends MigratorAction {

    public StatusAction() {
        super("Change Log Status");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ideFacade.getMigrator(database).reportStatus(true, new PrintStream(byteArrayOutputStream));

        ideFacade.displayOutput("Change Log Status", byteArrayOutputStream.toString());

    }

}
