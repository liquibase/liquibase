package liquibase.migrator.ant;

import liquibase.migrator.MigrationFailedException;
import liquibase.migrator.Migrator;
import org.apache.tools.ant.BuildException;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseMigratorTask extends BaseLiquibaseTask {
    private boolean dropFirst = true;
    private String contexts;

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String cntx) {
        this.contexts = cntx;
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
            migrator.setContexts(getContexts());
            migrator.setMode(Migrator.EXECUTE_MODE);

            if (isPromptOnNonLocalDatabase() && !migrator.isSaveToRunMigration()) {
                if (JOptionPane.showConfirmDialog(null, "You are running a database refactoring against a non-local database.\n" +
                        "Database URL is: " + migrator.getDatabase().getConnectionURL() + "\n" +
                        "Username is: " + migrator.getDatabase().getConnectionUsername() + "\n\n" +
                        "Area you sure you want to do this?",
                        "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                    throw new MigrationFailedException("Chose not to run against non-production database");
                }
            }

            if (isDropFirst()) {
                migrator.dropAll();
            }
            migrator.migrate();
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            if (migrator != null && migrator.getDatabase() != null && migrator.getDatabase().getConnection() != null) {
                try {
                    migrator.getDatabase().getConnection().close();
                } catch (SQLException e) {
                    throw new BuildException(e);
                }
            }
        }
    }
}
