package liquibase.snapshot;

import liquibase.database.Database;
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

    DatabaseSnapshot createSnapshot(Database database, DiffControl diffControl, DiffControl.DatabaseRole type) throws DatabaseException;

    Table getDatabaseChangeLogTable(Database database) throws DatabaseException;

    Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException;

    Table getTable(Schema schema, String tableName, Database database) throws DatabaseException;

    Column getColumn(Schema schema, String tableName, String columnName, Database database) throws DatabaseException;

    boolean hasForeignKey(Schema schema, String tableName, String fkName, Database database) throws DatabaseException;

    boolean hasIndex(Schema schema, String tableName, String indexName, String columnNames, Database database) throws DatabaseException;

    boolean hasDatabaseChangeLogTable(Database database);

    boolean hasDatabaseChangeLogLockTable(Database database);

    public boolean hasTable(Schema schema, String tableName, Database database);
    
    public boolean hasView(Schema schema, String viewName, Database database);

    boolean hasPrimaryKey(Schema schema, String tableName, String primaryKeyName, Database database);

    boolean hasColumn(Schema schema, String tableName, String columnName, Database database);
}
