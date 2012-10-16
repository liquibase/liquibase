package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.util.*;

public class DatabaseSnapshot {

    private SnapshotControl snapshotControl;
    private Database database;
    private Set<Schema> schemas = new HashSet<Schema>();
    private Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> allFound = new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>();

    public DatabaseSnapshot(Database database, SnapshotControl snapshotControl) {
        this.database = database;
        this.snapshotControl = snapshotControl;
    }

    public SnapshotControl getSnapshotControl() {
        return snapshotControl;
    }

    public Database getDatabase() {
        return database;
    }

    public Set<Schema> getSchemas() {
        return Collections.unmodifiableSet(schemas);
    }

    public void addSchema(Schema schema) {
        schemas.add(schema);
    }

    public boolean hasDatabaseChangeLogTable() {
        return false;  //todo
    }

    public <DatabaseObjectType extends DatabaseObject> DatabaseObjectType snapshot(DatabaseObjectType example) throws DatabaseException, InvalidExampleException {
        DatabaseObjectType returnObj = getExistingObject(example);
        if (returnObj == null) {
            returnObj = SnapshotGeneratorFactory.getInstance().snapshot(example, this.getDatabase());
            if (returnObj == null) {
                return null;
            }
            Set<DatabaseObject> collection = allFound.get(returnObj.getClass());
            if (collection == null) {
                collection = new HashSet<DatabaseObject>();
                allFound.put(returnObj.getClass(), collection);
            }
            collection.add(returnObj);

            return returnObj;
        } else {
            return returnObj;
        }
    }

    public <DatabaseObjectType extends  DatabaseObject> Set<DatabaseObjectType> getAll(Class<DatabaseObjectType> type) {
        return (Set<DatabaseObjectType>) allFound.get(type);
    }

    private <DatabaseObjectType extends DatabaseObject> DatabaseObjectType getExistingObject(DatabaseObjectType example) {
        Set<DatabaseObject> databaseObjects = allFound.get(example.getClass());
        if (databaseObjects == null) {
            return null;
        }
        for (DatabaseObject obj : databaseObjects) {
            if (obj.equals(example, database)) {
                return (DatabaseObjectType) obj;
            }
        }
        return null;
    }

}
