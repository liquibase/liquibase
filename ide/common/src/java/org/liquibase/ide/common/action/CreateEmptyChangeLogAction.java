package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class CreateEmptyChangeLogAction extends MigratorAction {

    public CreateEmptyChangeLogAction() {
        super("Create Empty Change Log");
    }

    @Override
    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        String changeLogFile = ideFacade.promptForChangeLogFile();
        try {
            ideFacade.getChangeLogWriter().createEmptyChangeLog(changeLogFile);
        } catch (Exception e) {
            ideFacade.showError(e);
        }
    }

    @Override
    public boolean needsRefresh() {
        return false;
    }
}
