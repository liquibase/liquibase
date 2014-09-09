package liquibase.snapshot;

import liquibase.ExecutionEnvironment;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedException;
import liquibase.servicelocator.LiquibaseService;
import liquibase.structure.DatabaseObject;

import java.util.*;

@LiquibaseService(skip = true)
public class MockSnapshotLookupLogic implements SnapshotLookupLogic {

    private Map<DatabaseObject, List<DatabaseObject>> configuredObjects = new HashMap<DatabaseObject, List<DatabaseObject>>();
    private Map<Class, Integer> priorities = new HashMap<Class, Integer>();


    public MockSnapshotLookupLogic(Map<DatabaseObject, List<DatabaseObject>> objectsByContainer) {
        configuredObjects.putAll(objectsByContainer);
        for (List<DatabaseObject> list : objectsByContainer.values()) {
            for (DatabaseObject obj : list) {
                if (!priorities.containsKey(obj.getClass())) {
                    priorities.put(obj.getClass(), PRIORITY_DEFAULT);
                }
            }
        }
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment) {
        Integer priority = priorities.get(objectType);
        if (priority == null) {
            return PRIORITY_NONE;
        } else {
            return PRIORITY_DEFAULT;
        }
    }

    @Override
    public <T extends DatabaseObject> Collection<T> lookup(Class<T> objectType, DatabaseObject example, ExecutionEnvironment environment) throws DatabaseException, UnsupportedException {
        List<T> returnList = new ArrayList<T>();
        List<DatabaseObject> objects = configuredObjects.get(example);
        if (objects != null) {
            for (DatabaseObject obj : objects) {
                if (objectType.isAssignableFrom(obj.getClass()) && includeInLookup(obj, objectType, example, environment)) {
                    returnList.add((T) obj);
                }
            }
        }
        if (objectType.isAssignableFrom(example.getClass()) && configuredObjects.containsKey(example)) {
            returnList.add((T) example);
        }
        return returnList;
    }

    protected <T extends DatabaseObject> boolean includeInLookup(DatabaseObject obj, Class<T> objectType, DatabaseObject example, ExecutionEnvironment environment) {
        return true;
    }

}
