package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.util.ui.UIFactory;
import org.apache.tools.ant.BuildException;

import java.io.Writer;

/**
 * Ant task for migrating a database forward.
 */
public class DatabaseUpdateTask extends BaseLiquibaseTask {
    private boolean dropFirst = false;

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

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

            Writer writer = createOutputWriter();
            if (writer == null) {
                liquibase.update(getContexts());
            } else {
                liquibase.update(getContexts(), writer);
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
