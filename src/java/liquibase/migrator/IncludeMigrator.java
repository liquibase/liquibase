package liquibase.migrator;

import liquibase.database.Database;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Subclass of Migrator for running included change logs.
 *
 * @see Migrator
 */
public class IncludeMigrator extends Migrator {
    private Migrator parentMigrator;

    public IncludeMigrator(String file, Migrator parentMigrator) throws SQLException, MigrationFailedException {
        super(file, parentMigrator.getFileOpener(), true);
        this.parentMigrator = parentMigrator;
        setMode(parentMigrator.getMode());
        setRollbackToDate(parentMigrator.getRollbackToDate());
        setRollbackToTag(parentMigrator.getRollbackToTag());
        setRollbackCount(parentMigrator.getRollbackCount());
        setOutputSQLWriter(parentMigrator.getOutputSQLWriter());
    }

    public Database getDatabase() {
        return parentMigrator.getDatabase();
    }

    /**
     * No-op, included files don't need a lock, the parent migrator already has one
     */
    public boolean acquireLock() throws MigrationFailedException {
        return true;
    }

    /**
     * No-op, included files don't need a to release lock, the parent migrator will
     */
    public void releaseLock() throws MigrationFailedException {
        ;
    }

    public List<RanChangeSet> getRanChangeSetList() throws SQLException {
        return parentMigrator.getRanChangeSetList();
    }

    public Set<String> getContexts() {
        return parentMigrator.getContexts();
    }
}
