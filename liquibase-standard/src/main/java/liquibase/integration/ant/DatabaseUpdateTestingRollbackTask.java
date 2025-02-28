package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import lombok.Getter;
import lombok.Setter;
import org.apache.tools.ant.BuildException;

/**
 * Ant task for migrating a database forward testing rollback.
 */
@Getter
@Setter
public class DatabaseUpdateTestingRollbackTask extends AbstractChangeLogBasedTask {
    private boolean dropFirst;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            if (isDropFirst()) {
                liquibase.dropAll();
            }
            liquibase.updateTestingRollback(new Contexts(getContexts()), getLabelFilter());
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to update database with a rollback test: " + e.getMessage(), e);
        }
    }

}
