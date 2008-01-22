package liquibase.ant;

import liquibase.migrator.Migrator;
import liquibase.migrator.UIFactory;
import org.apache.tools.ant.BuildException;

import java.sql.SQLException;

/**
 * Ant task for migrating a database forward.
 */
public class DatabaseMigratorTask extends BaseLiquibaseTask {
    private boolean dropFirst = false;

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    public void execute() throws BuildException {
        String shouldRunProperty = System.getProperty(Migrator.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            log("Migrator did not run because '" + Migrator.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
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
            migrator.update(getContexts());
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            if (migrator != null && migrator.getDatabase() != null && migrator.getDatabase().getConnection() != null) {
                try {
                    migrator.getDatabase().getConnection().close();
                } catch (SQLException e) {
                    ;
                }
            }
        }
    }
}
