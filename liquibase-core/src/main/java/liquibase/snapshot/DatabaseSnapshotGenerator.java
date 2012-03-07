package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;
import liquibase.database.structure.Column;
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

    Table getTable(String catalog, String schemaName, String tableName, Database database) throws DatabaseException;

    Column getColumn(String catalog, String schemaName, String tableName, String columnName, Database database) throws DatabaseException;

    boolean hasForeignKey(String catalog, String schemaName, String tableName, String fkName, Database database) throws DatabaseException;

    boolean hasIndex(String catalog, String schemaName, String tableName, String indexName, Database database, String columnNames) throws DatabaseException;

    boolean hasDatabaseChangeLogTable(Database database);

    boolean hasDatabaseChangeLogLockTable(Database database);

    public boolean hasTable(String catalog, String schemaName, String tableName, Database database);
    
    public boolean hasView(String catalog, String schemaName, String viewName, Database database);

    boolean hasPrimaryKey(Schema schema, String tableName, String primaryKeyName, Database database);

    boolean hasColumn(String catalogName, String schemaName, String tableName, String columnName, Database database);
}
