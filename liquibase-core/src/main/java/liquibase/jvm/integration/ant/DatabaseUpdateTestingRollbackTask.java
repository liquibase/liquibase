package liquibase.jvm.integration.ant;

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
    public void execute() throws BuildException {
        if (!shouldRun()) {
            return;
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase();

            if (isPromptOnNonLocalDatabase()
                    && !liquibase.isSafeToRunMigration()
                    && UIFactory.getInstance().getFacade().promptForNonLocalDatabase(liquibase.getDatabase())) {
                throw new BuildException("Chose not to run against non-production database");
            }

            if (isDropFirst()) {
                liquibase.dropAll();
            }

            liquibase.updateTestingRollback(getContexts());

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(liquibase);
        }
    }
}