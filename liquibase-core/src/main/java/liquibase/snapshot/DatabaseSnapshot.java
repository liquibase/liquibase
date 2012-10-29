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
    private Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> allFound = new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>();
    private Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> knownNull = new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>();

    DatabaseSnapshot(SnapshotControl snapshotControl, Database database) {
        this.database = database;
        this.snapshotControl = snapshotControl;
    }

    public DatabaseSnapshot(Database database) {
        this.database = database;
    }

    public SnapshotControl getSnapshotControl() {
        return snapshotControl;
    }

    public Database getDatabase() {
        return database;
    }

    /**
     * Include the object described by the passed example object in this snapshot. Returns the object snapshot or null if the object does not exist in the database.
     * If the same object was returned by an earlier include() call, the same object instance will be returned.
     */
    public <T extends DatabaseObject> T include(T example) throws DatabaseException, InvalidExampleException {
        T existing = get(example);
        if (existing != null) {
            return existing;
        }
        if (isKnownNull(example)) {
            return null;
        }

        SnapshotGeneratorChain chain = createGeneratorChain(example.getClass(), database);
        T object = chain.snapshot(example, this);
        if (object == null) {
            Set<DatabaseObject> collection = knownNull.get(example.getClass());
            if (collection == null) {
                collection = new HashSet<DatabaseObject>();
                knownNull.put(example.getClass(), collection);
            }
            collection.add(example);
            return null;
        }
        Set<DatabaseObject> collection = allFound.get(object.getClass());
        if (collection == null) {
            collection = new HashSet<DatabaseObject>();
            allFound.put(object.getClass(), collection);
        }
        collection.add(object);
        return  object;
    }

    /**
     * Returns the object described by the passed example if it is already included in this snapshot.
     */
    public <DatabaseObjectType extends DatabaseObject> DatabaseObjectType get(DatabaseObjectType example) {
        Set<DatabaseObject> databaseObjects = allFound.get(example.getClass());
        if (databaseObjects == null) {
            return null;
        }
        for (DatabaseObject obj : databaseObjects) {
            if (obj.equals(example, database)) {
                //noinspection unchecked
                return (DatabaseObjectType) obj;
            }
        }
        return null;
    }

    /**
     * Returns all objects of the given type that are already included in this snapshot.
     */
    public <DatabaseObjectType extends  DatabaseObject> Set<DatabaseObjectType> get(Class<DatabaseObjectType> type) {
        //noinspection unchecked
        return Collections.unmodifiableSet((Set<DatabaseObjectType>) allFound.get(type));
    }


    private SnapshotGeneratorChain createGeneratorChain(Class<? extends DatabaseObject> databaseObjectType, Database database) {
        SortedSet<SnapshotGenerator> generators = SnapshotGeneratorFactory.getInstance().getGenerators(databaseObjectType, database);
        if (generators == null || generators.size() == 0) {
            return null;
        }
        //noinspection unchecked
        return new SnapshotGeneratorChain(generators);
    }

    private boolean isKnownNull(DatabaseObject example) {
        Set<DatabaseObject> databaseObjects = knownNull.get(example.getClass());
        if (databaseObjects == null) {
            return false;
        }
        for (DatabaseObject obj : databaseObjects) {
            if (obj.equals(example, database)) {
                return true;
            }
        }
        return false;
    }
}
