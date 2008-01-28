package liquibase.ant;

import liquibase.database.Database;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.migrator.Migrator;
import org.apache.tools.ant.BuildException;

import java.io.PrintStream;

public class GenerateChangeLogTask extends BaseLiquibaseTask {

    public void execute() throws BuildException {
        Migrator migrator = null;
        try {
            PrintStream writer = createPrintStream();
            if (writer == null) {
                throw new BuildException("generateChangeLog requires outputFile to be set");
            }

            migrator = createMigrator();

            Database database = migrator.getDatabase();
            Diff diff = new Diff(database, getDefaultSchemaName());
//            diff.addStatusListener(new OutDiffStatusListener());
            DiffResult diffResult = diff.compare();

            diffResult.printChangeLog(writer, database);

            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(migrator);
        }
    }
}
