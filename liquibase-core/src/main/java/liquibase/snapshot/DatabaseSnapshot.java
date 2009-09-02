package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.structure.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DatabaseSnapshot {

    private Database database;
    private Set<Table> tables = new HashSet<Table>();
    private Set<View> views = new HashSet<View>();
    private Set<ForeignKey> foreignKeys = new HashSet<ForeignKey>();
    private Set<UniqueConstraint> uniqueConstraints = new HashSet<UniqueConstraint>();
    private Set<Index> indexes = new HashSet<Index>();
    private Set<PrimaryKey> primaryKeys = new HashSet<PrimaryKey>();
    private Set<Sequence> sequences = new HashSet<Sequence>();

    private String schema;

    private Table databaseChangeLogTable;
    private Table databaseChangeLogLockTable;

    public DatabaseSnapshot(Database database, String requestedSchema) {
        this.database = database;
        this.schema = requestedSchema;
    }


    public Database getDatabase() {
        return database;
    }

    public Set<Table> getTables() {
        return tables;
    }

    public Set<View> getViews() {
        return views;
    }

    public Set<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public Set<Index> getIndexes() {
        return indexes;
    }

    public Set<PrimaryKey> getPrimaryKeys() {
        return primaryKeys;
    }


    public Set<Sequence> getSequences() {
        return sequences;
    }

    public Set<UniqueConstraint> getUniqueConstraints() {
        return this.uniqueConstraints;
    }

    public Table getTable(String tableName) {
        for (Table table : getTables()) {
            if (table.getName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        return null;
    }

    public ForeignKey getForeignKey(String foreignKeyName) {
        for (ForeignKey fk : getForeignKeys()) {
            if (fk.getName().equalsIgnoreCase(foreignKeyName)) {
                return fk;
            }
        }
        return null;
    }

    public Sequence getSequence(String sequenceName) {
        for (Sequence sequence : getSequences()) {
            if (sequence.getName().equalsIgnoreCase(sequenceName)) {
                return sequence;
            }
        }
        return null;
    }

    public Index getIndex(String indexName) {
        for (Index index : getIndexes()) {
            if (index.getName().equalsIgnoreCase(indexName)) {
                return index;
            }
        }
        return null;
    }

    public View getView(String viewName) {
        for (View view : getViews()) {
            if (view.getName().equalsIgnoreCase(viewName)) {
                return view;
            }
        }
        return null;
    }

    public PrimaryKey getPrimaryKey(String pkName) {
        for (PrimaryKey pk : getPrimaryKeys()) {
            if (pk.getName().equalsIgnoreCase(pkName)) {
                return pk;
            }
        }
        return null;
    }

    public PrimaryKey getPrimaryKeyForTable(String tableName) {
        for (PrimaryKey pk : getPrimaryKeys()) {
            if (pk.getTable().getName().equalsIgnoreCase(tableName)) {
                return pk;
            }
        }
        return null;
    }

    public UniqueConstraint getUniqueConstraint(String ucName) {
        for (UniqueConstraint uc : getUniqueConstraints()) {
            if (uc.getName().equalsIgnoreCase(ucName)) {
                return uc;
            }
        }
        return null;
    }

    public String getSchema() {
        return schema;
    }

    public boolean isPrimaryKey(Column columnInfo) {
        for (PrimaryKey pk : getPrimaryKeys()) {
            if (columnInfo.getTable() == null) {
                continue;
            }
            if (pk.getTable().getName().equalsIgnoreCase(columnInfo.getTable().getName())) {
                if (pk.getColumnNamesAsList().contains(columnInfo.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public Collection<Column> getColumns() {
        Set<Column> returnSet = new HashSet<Column>();

        for (Table table : getTables()) {
            for (Column column : table.getColumns()) {
                returnSet.add(column);
            }
        }

        return returnSet;
    }

    public Column getColumn(String tableName, String columnName) {
        for (Table table : getTables()) {
            for (Column column : table.getColumns()) {
                if (table.getName().equalsIgnoreCase(tableName) && column.getName().equalsIgnoreCase(columnName)) {
                    return column;
                }
            }
        }
        return null;
    }

    public boolean hasDatabaseChangeLogTable() {
        return databaseChangeLogTable != null;
    }

    public Table getDatabaseChangeLogTable() {
        return databaseChangeLogTable;
    }

    public void setDatabaseChangeLogTable(Table table) {
        this.databaseChangeLogTable = table;
    }

    public Table getDatabaseChangeLogLockTable() {
        return databaseChangeLogLockTable;
    }

    public void setDatabaseChangeLogLockTable(Table table) {
        this.databaseChangeLogLockTable = table;
    }
}
