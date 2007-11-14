package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class GenerateChangelogAction extends MigratorAction {

    public GenerateChangelogAction() {
        super("Generate Complete Change Log");
    }

    public void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException {
        Diff diff = new Diff(database.getConnection());
//        diff.addStatusListener(new OutDiffStatusListener());
        DiffResult diffResult = diff.compare();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            diffResult.printChangeLog(new PrintStream(out), database);
        } catch (ParserConfigurationException e) {
            throw new LiquibaseException(e);
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        ideFacade.showOutput("Rollback SQL", out.toString());
    }


    public boolean needsRefresh() {
        return false;
    }


}
