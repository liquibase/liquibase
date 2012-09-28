package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.Column;
import liquibase.diff.DiffControl;
import liquibase.exception.DatabaseException;

public interface DatabaseSnapshotGenerator {
    /**
     * Default generator, lower priority.
     */
    public static final int PRIORITY_DEFAULT = 1;

    /**
     * Generator specific to database, higher priority.
     *
     */
    public static final int PRIORITY_DATABASE = 5;

    boolean supports(Database database);

    int getPriority(Database database);

    DatabaseSnapshot createSnapshot(SnapshotControl snapshotControl) throws DatabaseException;

    Table getDatabaseChangeLogTable() throws DatabaseException;

    Table getDatabaseChangeLogLockTable() throws DatabaseException;

    boolean hasDatabaseChangeLogTable() throws DatabaseException;

    boolean hasDatabaseChangeLogLockTable() throws DatabaseException;
}
