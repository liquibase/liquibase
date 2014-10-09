package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;

/**
 * Ant task for migrating a database forward testing rollback.
 */
public class DatabaseUpdateTestingRollbackTask extends AbstractChangeLogBasedTask {
    private boolean dropFirst = false;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            if (isDropFirst()) {
                liquibase.dropAll();
            }
            liquibase.updateTestingRollback(new Contexts(getContexts()), getLabels());
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to update database with a rollback test.", e);
        }
    }

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }
}