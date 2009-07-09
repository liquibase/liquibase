package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;

public class GenerateChangeLogTask extends BaseLiquibaseTask {

    @Override
    public void execute() throws BuildException {
        Liquibase liquibase = null;
        try {
            PrintStream writer = createPrintStream();
            if (writer == null) {
                throw new BuildException("generateChangeLog requires outputFile to be set");
            }

            liquibase = createLiquibase();

            Database database = liquibase.getDatabase();
            Diff diff = new Diff(database, getDefaultSchemaName());
//            diff.addStatusListener(new OutDiffStatusListener());
            DiffResult diffResult = diff.compare();

            if (getChangeLogFile() == null) {
                diffResult.printChangeLog(writer, database);
            } else {
                diffResult.printChangeLog(getChangeLogFile(), database);
            }

            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}
