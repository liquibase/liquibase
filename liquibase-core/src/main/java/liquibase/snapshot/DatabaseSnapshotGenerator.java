package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.structure.Table;
import liquibase.database.structure.Column;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.PrioritizedService;

import java.util.Set;

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

    DatabaseSnapshot createSnapshot(Database database, String schema, Set<DiffStatusListener> listeners) throws DatabaseException;

    boolean hasDatabaseChangeLogTable(Database database) throws DatabaseException;

    boolean hasDatabaseChangeLogLockTable(Database database) throws DatabaseException;

    Table getTable(String schemaName, String tableName, Database database) throws DatabaseException;

    Column getColumn(String schemaName, String tableName, String columnName, Database database) throws DatabaseException;
}
