package liquibase.ant;

import liquibase.migrator.Migrator;
import org.apache.tools.ant.BuildException;

import java.io.Writer;

/**
 * Ant task for rolling back a database.
 */
public class DatabaseRollbackFutureTask extends BaseLiquibaseTask {

    public void execute() throws BuildException {
        Migrator migrator = null;
        try {
            Writer writer = createOutputWriter();
            if (writer == null) {
                throw new BuildException("rollbackFutureDatabase requires outputFile to be set");
            }

            migrator = createMigrator();


            migrator.futureRollbackSQL(getContexts(), writer);

            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(migrator);
        }
    }
}