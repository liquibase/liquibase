package liquibase.snapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.PrimaryKey;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;

public class DatabaseSnapshot {

    private Database database;

    private Table databaseChangeLogTable;
    private Table databaseChangeLogLockTable;
    
    private Map<Schema, SchemaSnapshot> schemaSnapshots = new HashMap<Schema, SchemaSnapshot>();

    public DatabaseSnapshot(Database database) {
        this.database = database;
    }


    public Database getDatabase() {
        return database;
    }

    public Set<Schema> getSchemas() {
        return Collections.unmodifiableSet(schemaSnapshots.keySet());
    }

    public <T extends DatabaseObject> Set<T> getDatabaseObjects(Schema schema, Class<T> type) {
        schema = schema.clone(database);
        if (!schemaSnapshots.containsKey(schema)) {
            return Collections.unmodifiableSet(new HashSet<T>());
        }
        Set<? extends DatabaseObject> snapshotItems = schemaSnapshots.get(schema).databaseObjects.get(type);
        if (snapshotItems == null) {
            return Collections.unmodifiableSet(new HashSet<T>());
        }

        //noinspection unchecked
        return (Set<T>) Collections.unmodifiableSet(snapshotItems);
    }

    public <T extends DatabaseObject> T getDatabaseObject(Schema schema, String objectName, Class<T> type) {
        for (DatabaseObject object : getDatabaseObjects(schema, type)) {
            if (object.getName().equals(objectName)) {
                //noinspection unchecked
                return (T) object;
            }
        }
        return null;
    }

    public <T extends DatabaseObject> T getDatabaseObject(Schema schema, DatabaseObject databaseObject, Class<T> type) {
        for (DatabaseObject object : getDatabaseObjects(schema, type)) {
            if (object.equals(databaseObject)) {
                //noinspection unchecked
                return (T) object;
            }
        }
        return null;
    }

    public void addDatabaseObjects(DatabaseObject... objects) {

        for (DatabaseObject object : objects) {
            Schema schema = object.getSchema();
            
            if (!schemaSnapshots.containsKey(schema)) {
                schemaSnapshots.put(schema, new SchemaSnapshot());
            }
            SchemaSnapshot schemaSnapshot = schemaSnapshots.get(schema);

            if (!schemaSnapshot.databaseObjects.containsKey(object.getClass())) {
                schemaSnapshot.databaseObjects.put(object.getClass(), new HashSet<DatabaseObject>());
            }

            schemaSnapshot.databaseObjects.get((object.getClass())).add(object);
        }
    }

    public void removeDatabaseObjects(Schema schema, DatabaseObject... objects) {
        SchemaSnapshot schemaSnapshot = schemaSnapshots.get(schema);
        if (schemaSnapshot == null) {
            return;
        }

        for (DatabaseObject object : objects) {
            if (!schemaSnapshot.databaseObjects.containsKey(object.getClass())) {
                return;
            }

            schemaSnapshot.databaseObjects.get((object.getClass())).remove(object);
        }
    }

    public boolean isPrimaryKey(Column column) {
        for (PrimaryKey pk : getDatabaseObjects(column.getRelation().getSchema(), PrimaryKey.class)) {
            if (column.getRelation() == null) {
                continue;
            }
            if (pk.getTable().getName().equalsIgnoreCase(column.getRelation().getName())) {
                if (pk.getColumnNamesAsList().contains(column.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public PrimaryKey getPrimaryKeyForTable(Schema schema, String tableName) {
        Table table = getDatabaseObject(schema, tableName, Table.class);
        if (table == null) {
            return null;
        }
        return table.getPrimaryKey();

    }


    public Set<Column> getColumns(Schema schema) {
        Set<Column> returnSet = new HashSet<Column>();

        for (Table table : getDatabaseObjects(schema, Table.class)) {
            for (Column column : table.getColumns()) {
                returnSet.add(column);
            }
        }

        return Collections.unmodifiableSet(returnSet);
    }

    public Column getColumn(Schema schema, String tableName, String columnName) {
        Table table = getDatabaseObject(schema, tableName, Table.class);

        return table.getColumn(columnName);
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

    public boolean contains(Schema schema, DatabaseObject databaseObject) {
        return schemaSnapshots.containsKey(schema)
                && schemaSnapshots.get(schema).databaseObjects.containsKey(databaseObject.getClass())
                && schemaSnapshots.get(schema).databaseObjects.get(databaseObject.getClass()).contains(databaseObject);

    }

    public boolean matches(Schema schema, DatabaseObject databaseObject) {
        DatabaseObject thisDatabaseObject = this.getDatabaseObject(schema, databaseObject, databaseObject.getClass());
        return thisDatabaseObject != null;

    }

    private static class SchemaSnapshot {
        private Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> databaseObjects = new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>();

    }
}
