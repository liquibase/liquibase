package liquibase.ant;

import liquibase.database.Database;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.Liquibase;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;

public class GenerateChangeLogTask extends BaseLiquibaseTask {

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

            diffResult.printChangeLog(writer, database);

            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}
