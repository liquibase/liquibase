package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;

import java.io.IOException;

public class DatabaseRollbackFutureTask extends AbstractChangeLogBasedTask {
    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            liquibase.futureRollbackSQL(null, new Contexts(getContexts()), getLabels(), getOutputFileWriter());
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to generate future rollback SQL.", e);
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