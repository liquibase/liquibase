package liquibase.ant;

import liquibase.migrator.Migrator;
import liquibase.migrator.UIFactory;
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

        Migrator migrator = null;
        try {
            migrator = createMigrator();

            if (isPromptOnNonLocalDatabase()
                    && !migrator.isSafeToRunMigration()
                    && UIFactory.getInstance().getFacade().promptForNonLocalDatabase(migrator.getDatabase())) {
                throw new BuildException("Chose not to run against non-production database");
            }

            if (isDropFirst()) {
                migrator.dropAll();
            }

            Writer writer = createOutputWriter();
            if (writer == null) {
                migrator.update(getContexts());
            } else {
                migrator.update(getContexts(), writer);
                writer.flush();
                writer.close();
            }

        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            closeDatabase(migrator);
        }
    }
}
