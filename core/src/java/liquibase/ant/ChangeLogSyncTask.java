package liquibase.ant;

import liquibase.migrator.Migrator;
import org.apache.tools.ant.BuildException;

import java.io.Writer;

public class ChangeLogSyncTask extends BaseLiquibaseTask {

    public void execute() throws BuildException {

        Migrator migrator = null;
        try {
            migrator = createMigrator();

            Writer writer = createOutputWriter();
            if (writer == null) {
                migrator.changeLogSync(getContexts());
            } else {
                migrator.changeLogSync(getContexts(), writer);
                writer.flush();
                writer.close();
            }

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(migrator);
        }
    }
}