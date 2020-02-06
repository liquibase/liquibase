package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;

import java.io.IOException;
import java.io.Writer;

public class DatabaseRollbackFutureTask extends AbstractChangeLogBasedTask {
    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try (Writer outputFileWriter = getOutputFileWriter()) {
            liquibase.futureRollbackSQL(new Contexts(getContexts()), getLabels(), outputFileWriter);
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to generate future rollback SQL: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new BuildException("Unable to generate future rollback SQL. Error creating output writer.", e);
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(getOutputFile() == null) {
            throw new BuildException("Output file is required.");
        }
    }
}
