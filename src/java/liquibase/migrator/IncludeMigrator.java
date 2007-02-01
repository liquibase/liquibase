package liquibase.migrator;

import liquibase.database.AbstractDatabase;

import java.io.IOException;
import java.sql.SQLException;

public class IncludeMigrator extends Migrator {
    private Migrator parentMigrator;

    public IncludeMigrator(String file, Migrator parentMigrator) throws SQLException, MigrationFailedException {
        super(file, parentMigrator.getFileOpener(), true);
        this.parentMigrator = parentMigrator;
        setMode(parentMigrator.getMode());
        setOutputSQLWriter(parentMigrator.getOutputSQLWriter());
    }

    public AbstractDatabase getDatabase() {
        return parentMigrator.getDatabase();
    }

    /**
     * No-op, included files don't need a lock, the parent migrator already has one
     */
    protected boolean aquireLock() throws MigrationFailedException {
        return true;
    }

    /**
     * No-op, included files don't need a to release lock, the parent migrator will
     */
    protected void releaseLock() throws MigrationFailedException {
        ;
    }
}
