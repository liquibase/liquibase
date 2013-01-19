package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;

import java.lang.reflect.Field;
import java.util.*;

public abstract class DatabaseSnapshot {

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
        this.snapshotControl = new SnapshotControl();
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
//    public void include(DatabaseObject example) throws DatabaseException, InvalidExampleException {
//        include(example, false);
//    }

    protected  <T extends DatabaseObject> T include(T example) throws DatabaseException, InvalidExampleException {
        if (database.isSystemObject(example)) {
            return null;
        }

        if (example instanceof Schema && example.getName() == null && (((Schema) example).getCatalog() == null || ((Schema) example).getCatalogName() == null)) {
            CatalogAndSchema catalogAndSchema = database.correctSchema(((Schema) example).toCatalogAndSchema());
            example = (T) new Schema(catalogAndSchema.getCatalogName(), catalogAndSchema.getSchemaName());
        }

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

        } else {
            Set<DatabaseObject> collection = allFound.get(object.getClass());
            if (collection == null) {
                collection = new HashSet<DatabaseObject>();
                allFound.put(object.getClass(), collection);
            }
            collection.add(object);

            try {
                includeNestedObjects(object);
            } catch (InstantiationException e) {
                throw new UnexpectedLiquibaseException(e);
            } catch (IllegalAccessException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return object;
    }

    private void includeNestedObjects(DatabaseObject object) throws DatabaseException, InvalidExampleException, InstantiationException, IllegalAccessException {
            for (String field : new HashSet<String>(object.getAttributes())) {
                Object fieldValue = object.getAttribute(field, Object.class);
                Object newFieldValue = replaceObject(fieldValue);
                if (fieldValue != newFieldValue) {
                    object.setAttribute(field, newFieldValue);
                }

            }
    }

    private Object replaceObject(Object fieldValue) throws DatabaseException, InvalidExampleException, IllegalAccessException, InstantiationException {
        if (fieldValue == null) {
            return null;
        }
        if (fieldValue instanceof DatabaseObject) {
            if (((DatabaseObject) fieldValue).getSnapshotId() == null) {
                return include((DatabaseObject) fieldValue);
            } else {
                return fieldValue;
            }
            //            } else if (Set.class.isAssignableFrom(field.getType())) {
            //                field.setAccessible(true);
            //                Set fieldValue = field.get(object);
            //                for (Object val : fieldValue) {
            //
            //                }
        } else if (fieldValue instanceof Collection) {
            Iterator fieldValueIterator = ((Collection) fieldValue).iterator();
            List newValues = new ArrayList();
            while (fieldValueIterator.hasNext()) {
                Object obj = fieldValueIterator.next();
                if (obj instanceof DatabaseObject && ((DatabaseObject) obj).getSnapshotId() == null) {
                    obj = include((DatabaseObject) obj);
                }
                if (obj != null) {
                    newValues.add(obj);
                }
            }
            Collection newCollection = (Collection) fieldValue.getClass().newInstance();
            newCollection.addAll(newValues);
            return newCollection;
        } else if (fieldValue instanceof Map) {
            Map newMap = (Map) fieldValue.getClass().newInstance();
            for (Map.Entry entry : (Set<Map.Entry>) ((Map) fieldValue).entrySet()) {
                Object key = replaceObject(entry.getKey());
                Object value = replaceObject(entry.getValue());

                if (key != null) {
                    newMap.put(key, value);
                }
            }

            return newMap;

        }

        return fieldValue;
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
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(obj, example, database)) {
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
        Set<DatabaseObjectType> objects = (Set<DatabaseObjectType>) allFound.get(type);
        if (objects == null) {
            return Collections.unmodifiableSet(new HashSet<DatabaseObjectType>());
        } else {
            return Collections.unmodifiableSet(objects);
        }
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
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(obj, example, database)) {
                return true;
            }
        }
        return false;
    }
}
