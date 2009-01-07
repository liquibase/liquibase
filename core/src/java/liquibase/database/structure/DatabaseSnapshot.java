package liquibase.database.structure;

import liquibase.database.Database;

import java.util.Set;

public interface DatabaseSnapshot {
    Database getDatabase();

    Set<Table> getTables();

    Set<View> getViews();

    Column getColumn(Column column);

    Column getColumn(String tableName, String columnName);

    Set<Column> getColumns();

    Set<ForeignKey> getForeignKeys();

    Set<Index> getIndexes();

    Set<PrimaryKey> getPrimaryKeys();

    Set<Sequence> getSequences();
    
    Set<UniqueConstraint> getUniqueConstraints();

    /**
     * Returns the table object for the given tableName.  If table does not exist, returns null
     */
    Table getTable(String tableName);

    ForeignKey getForeignKey(String foreignKeyName);

    Sequence getSequence(String sequenceName);

    Index getIndex(String indexName);

    View getView(String viewName);

    PrimaryKey getPrimaryKey(String pkName);

    PrimaryKey getPrimaryKeyForTable(String tableName);
    
    UniqueConstraint getUniqueConstraint (String ucName);

    String getSchema();

    boolean hasDatabaseChangeLogTable();
}
