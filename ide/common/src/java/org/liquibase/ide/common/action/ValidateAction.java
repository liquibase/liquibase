package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import org.liquibase.ide.common.IdeFacade;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ValidateAction extends MigratorAction {

    public ValidateAction() {
        super("Validate Change Log");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        String message;

        try {
            String changeLogFile = ideFacade.selectChangeLogFile();
            if (changeLogFile == null) {
                return;
            }

            ideFacade.getMigrator(changeLogFile, database).validate();
            message = "No validation errors found";
        } catch (ValidationFailedException e) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            e.printDescriptiveError(new PrintStream(byteArrayOutputStream));
            message = byteArrayOutputStream.toString();
        } catch (Exception e) {
            throw new LiquibaseException(e);
        }

        ideFacade.displayOutput("Change Log Status", message);

    }

    public boolean needsRefresh() {
        return false;
    }


}
