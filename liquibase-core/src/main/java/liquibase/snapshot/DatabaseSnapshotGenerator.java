package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.AbstractDatabase;
import liquibase.database.structure.Table;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKey;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.PrioritizedService;

import java.util.Set;
import java.util.List;

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

    Table getDatabaseChangeLogTable(Database database) throws DatabaseException;

    Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException;

    Table getTable(String schemaName, String tableName, Database database) throws DatabaseException;

    Column getColumn(String schemaName, String tableName, String columnName, Database database) throws DatabaseException;

    ForeignKey getForeignKeyByForeignKeyTable(String schemaName, String tableName, String fkName, Database database) throws DatabaseException;

    List<ForeignKey> getForeignKeys(String schemaName, String tableName, Database database) throws DatabaseException;

    boolean hasIndex(String schemaName, String tableName, String indexName, Database database, String columnNames) throws DatabaseException;

    boolean hasDatabaseChangeLogTable(Database database);

    boolean hasDatabaseChangeLogLockTable(Database database);

    public boolean hasTable(String schemaName, String tableName, Database database);
}
