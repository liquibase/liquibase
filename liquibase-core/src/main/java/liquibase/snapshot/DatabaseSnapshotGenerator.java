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

    DatabaseSnapshot createSnapshot(Database database, SnapshotControl snapshotControl) throws DatabaseException;

    Table getDatabaseChangeLogTable(Database database) throws DatabaseException;

    Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException;

    boolean hasDatabaseChangeLogTable(Database database) throws DatabaseException;

    boolean hasDatabaseChangeLogLockTable(Database database) throws DatabaseException;
}
