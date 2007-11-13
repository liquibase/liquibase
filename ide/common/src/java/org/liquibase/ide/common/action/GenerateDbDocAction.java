package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;

import java.io.File;
import java.io.IOException;

public class GenerateDbDocAction extends MigratorAction {

    public GenerateDbDocAction() {
        super("Generate DbDoc");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        File input = ideFacade.promptForDirectory(getTitle(), "Select Output Directory", null);
        if (input != null) {
            try {
                ideFacade.getMigrator(database).generateDocumentation(input.getCanonicalPath());
            } catch (IOException e) {
                throw new LiquibaseException(e);
            }
        }

    }

}
