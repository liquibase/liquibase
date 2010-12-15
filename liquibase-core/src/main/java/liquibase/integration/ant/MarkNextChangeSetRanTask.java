package liquibase.integration.ant;

import liquibase.Liquibase;
import org.apache.tools.ant.BuildException;

import java.io.Writer;

public class MarkNextChangeSetRanTask extends BaseLiquibaseTask {

    @Override
    public void execute() throws BuildException {

        super.execute();

        if (!shouldRun()) {
            return;
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();

            Writer writer = createOutputWriter();
            if (writer == null) {
                liquibase.markNextChangeSetRan(getContexts());
            } else {
                liquibase.markNextChangeSetRan(getContexts(), writer);
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