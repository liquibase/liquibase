package liquibase.integration.ant;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.util.ui.UIFactory;
import org.apache.tools.ant.BuildException;

/**
 * Ant task for migrating a database forward testing rollback.
 */
public class DatabaseUpdateTestingRollbackTask extends BaseLiquibaseTask {
    private boolean dropFirst = false;

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        if (!shouldRun()) {
            return;
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();

            if (isPromptOnNonLocalDatabase()
                    && !liquibase.isSafeToRunUpdate()
                    && UIFactory.getInstance().getFacade().promptForNonLocalDatabase(liquibase.getDatabase())) {
                throw new BuildException("Chose not to run against non-production database");
            }

            if (isDropFirst()) {
                liquibase.dropAll();
            }

            liquibase.updateTestingRollback(new Contexts(getContexts()), new LabelExpression(getLabels()));

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}