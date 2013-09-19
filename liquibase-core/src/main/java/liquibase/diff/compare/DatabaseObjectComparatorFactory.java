package liquibase.diff.compare;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

public class DatabaseObjectComparatorFactory {

    private static DatabaseObjectComparatorFactory instance;

    private List<DatabaseObjectComparator> comparators = new ArrayList<DatabaseObjectComparator>();

    private Map<String, List<DatabaseObjectComparator>> validComparatorsByClassAndDatabase = new HashMap<String, List<DatabaseObjectComparator>>();
    private Map<String, DatabaseObjectComparatorChain> comparatorChainsByClassAndDatabase = new HashMap<String, DatabaseObjectComparatorChain>();

    private DatabaseObjectComparatorFactory() {
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(DatabaseObjectComparator.class);

            for (Class clazz : classes) {
                register((DatabaseObjectComparator) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Return singleton DatabaseObjectComparatorFactory
     */
    public static DatabaseObjectComparatorFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseObjectComparatorFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new DatabaseObjectComparatorFactory();
    }


    public void register(DatabaseObjectComparator generator) {
        comparators.add(generator);
    }

    public void unregister(DatabaseObjectComparator generator) {
        comparators.remove(generator);
    }

    public void unregister(Class generatorClass) {
        DatabaseObjectComparator toRemove = null;
        for (DatabaseObjectComparator existingGenerator : comparators) {
            if (existingGenerator.getClass().equals(generatorClass)) {
                toRemove = existingGenerator;
            }
        }

        unregister(toRemove);
    }

    protected List<DatabaseObjectComparator> getComparators(Class<? extends DatabaseObject> comparatorClass, Database database) {
        String key = comparatorClass.getName()+":"+database.getShortName();
        if (validComparatorsByClassAndDatabase.containsKey(key)) {
            return validComparatorsByClassAndDatabase.get(key);
        }

        List<DatabaseObjectComparator> validComparators = new ArrayList<DatabaseObjectComparator>();

        for (DatabaseObjectComparator comparator : comparators) {
            if (comparator.getPriority(comparatorClass, database) > 0) {
                validComparators.add(comparator);
            }
        }

        Collections.sort(validComparators, new DatabaseObjectComparatorComparator(comparatorClass, database));

        validComparatorsByClassAndDatabase.put(key, validComparators);

        return validComparators;
    }


    public static void resetAll() {
        instance = null;
    }

    public boolean isSameObject(DatabaseObject object1, DatabaseObject object2, Database accordingTo) {
        if (object1 == null && object2 == null) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }

        UUID snapshotId1 = object1.getSnapshotId();
        UUID snapshotId2 = object2.getSnapshotId();
        if (snapshotId1 != null && snapshotId2 != null) {
            if (snapshotId1.equals(snapshotId2)) {
                return true;
            }
        }

        if (!hash(object1, accordingTo).equals(hash(object2, accordingTo))) {
            return false;
        }


        return createComparatorChain(object1.getClass(), accordingTo).isSameObject(object1, object2, accordingTo);
    }

    public String hash(DatabaseObject databaseObject, Database accordingTo) {
        String hash = null;
        if (databaseObject != null) {
            hash = createComparatorChain(databaseObject.getClass(), accordingTo).hash(databaseObject, accordingTo);
        }

        if (StringUtils.trimToNull(hash) == null) {
            hash = "null";
        }
        return hash;
    }

    public ObjectDifferences findDifferences(DatabaseObject object1, DatabaseObject object2, Database accordingTo) {
        return createComparatorChain(object1.getClass(), accordingTo).findDifferences(object1, object2, accordingTo);

    }

    private DatabaseObjectComparatorChain createComparatorChain(Class<? extends DatabaseObject> databaseObjectType, Database database) {
        String key = databaseObjectType.getName()+":"+database.getShortName();

        if (comparatorChainsByClassAndDatabase.containsKey(key)) {
            return comparatorChainsByClassAndDatabase.get(key).copy();
        }

        List<DatabaseObjectComparator> comparators = DatabaseObjectComparatorFactory.getInstance().getComparators(databaseObjectType, database);
        if (comparators == null || comparators.size() == 0) {
            return null;
        }

        DatabaseObjectComparatorChain chain = new DatabaseObjectComparatorChain(comparators);
        comparatorChainsByClassAndDatabase.put(key, chain);
        //noinspection unchecked
        return chain;
    }

}
