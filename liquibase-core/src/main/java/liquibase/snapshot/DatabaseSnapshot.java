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

    public Set<Schema> getSchemas() {
        return Collections.unmodifiableSet(schemas);
    }

    public void addSchema(Schema schema) {
        schemas.add(schema);
    }

    public boolean hasDatabaseChangeLogTable() {
        return false;  //todo
    }

    public <T extends DatabaseObject> T include(T example) throws DatabaseException, InvalidExampleException {
        T existing = get(example);
        if (existing != null) {
            return existing;
        }

        SnapshotGeneratorChain chain = createGeneratorChain(example.getClass(), database);
        T object = chain.snapshot(example, this);
        if (object == null) {
            return object;
        }
        Set<DatabaseObject> collection = allFound.get(object.getClass());
        if (collection == null) {
            collection = new HashSet<DatabaseObject>();
            allFound.put(object.getClass(), collection);
        }
        collection.add(object);
        return  object;
    }

    public boolean has(DatabaseObject example) {
        Set<DatabaseObject> databaseObjects = allFound.get(example.getClass());
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

    private SnapshotGeneratorChain createGeneratorChain(Class<? extends DatabaseObject> databaseObjectType, Database database) {
        SortedSet<SnapshotGenerator> generators = SnapshotGeneratorFactory.getInstance().getGenerators(databaseObjectType, database);
        if (generators == null || generators.size() == 0) {
            return null;
        }
        //noinspection unchecked
        return new SnapshotGeneratorChain(generators);
    }

    public <DatabaseObjectType extends  DatabaseObject> Set<DatabaseObjectType> getAll(Class<DatabaseObjectType> type) {
        return (Set<DatabaseObjectType>) allFound.get(type);
    }

    public  <DatabaseObjectType extends DatabaseObject> DatabaseObjectType get(DatabaseObjectType example) {
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
