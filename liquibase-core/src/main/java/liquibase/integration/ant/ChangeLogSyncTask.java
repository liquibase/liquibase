package liquibase.integration.ant;

import java.io.Writer;

import org.apache.tools.ant.BuildException;

import liquibase.Liquibase;

public class ChangeLogSyncTask extends BaseLiquibaseTask {

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        if (!shouldRun()) {
            return;
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();

            Writer writer = createOutputWriter();
            if (writer == null) {
                liquibase.changeLogSync(getContexts());
            } else {
                liquibase.changeLogSync(getContexts(), writer);
                writer.flush();
                writer.close();
            }

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}