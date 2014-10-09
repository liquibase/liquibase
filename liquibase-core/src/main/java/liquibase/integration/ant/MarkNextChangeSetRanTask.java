package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

import java.io.IOException;
import java.io.Writer;

public class MarkNextChangeSetRanTask extends AbstractChangeLogBasedTask {
    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        Writer writer = null;
        try {
            FileResource outputFile = getOutputFile();
            if (outputFile != null) {
                writer = getOutputFileWriter();
                liquibase.markNextChangeSetRan(new Contexts(getContexts()), getLabels(), writer);
            } else {
                liquibase.markNextChangeSetRan(new Contexts(getContexts()), getLabels());
            }
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to mark next changeset as ran.", e);
        } catch (IOException e) {
            throw new BuildException("Unable to mark next changeset as ran. Error creating output writer.", e);
        } finally {
            FileUtils.close(writer);
        }
    }
}