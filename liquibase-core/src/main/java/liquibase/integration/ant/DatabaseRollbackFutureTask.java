package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import org.apache.tools.ant.BuildException;

import java.io.Writer;

/**
 * Ant task for rolling back a database.
 */
public class DatabaseRollbackFutureTask extends BaseLiquibaseTask {

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = null;
        try {
            Writer writer = createOutputWriter();
            if (writer == null) {
                throw new BuildException("rollbackFutureDatabase requires outputFile to be set");
            }

            liquibase = createLiquibase();


            liquibase.futureRollbackSQL(null, new Contexts(getContexts()), new LabelExpression(getLabels()), writer);

            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}