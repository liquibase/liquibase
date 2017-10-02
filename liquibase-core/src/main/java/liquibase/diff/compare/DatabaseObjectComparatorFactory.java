package liquibase.diff.compare;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.util.*;

public class DatabaseObjectComparatorFactory {

    private static DatabaseObjectComparatorFactory instance;

    private List<DatabaseObjectComparator> comparators = new ArrayList<>();

    private Map<String, List<DatabaseObjectComparator>> validComparatorsByClassAndDatabase = new HashMap<>();
    private Map<String, DatabaseObjectComparatorChain> comparatorChainsByClassAndDatabase = new HashMap<>();

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
    public static synchronized DatabaseObjectComparatorFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseObjectComparatorFactory();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = new DatabaseObjectComparatorFactory();
    }

    public static synchronized void resetAll() {
        instance = null;
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

        List<DatabaseObjectComparator> validComparators = new ArrayList<>();

        for (DatabaseObjectComparator comparator : comparators) {
            if (comparator.getPriority(comparatorClass, database) > 0) {
                validComparators.add(comparator);
            }
        }

        Collections.sort(validComparators, new DatabaseObjectComparatorComparator(comparatorClass, database));

        validComparatorsByClassAndDatabase.put(key, validComparators);

        return validComparators;
    }

    public boolean isSameObject(DatabaseObject object1, DatabaseObject object2, CompareControl.SchemaComparison[] schemaComparisons, Database accordingTo) {
        if ((object1 == null) && (object2 == null)) {
            return true;
        }

        if ((object1 instanceof Schema) || (object2 instanceof Schema)) {
            if (object1 == null) {
                object1 = new Schema();
            }
            if (object2 == null) {
                object2 = new Schema();
            }
        } else if ((object1 instanceof Catalog) || (object2 instanceof Catalog)) {
                if (object1 == null) {
                    object1 = new Catalog();
                }
                if (object2 == null) {
                    object2 = new Catalog();
                }
        }
        if ((object1 == null) || (object2 == null)) {
            return false;
        }

        String snapshotId1 = object1.getSnapshotId();
        String snapshotId2 = object2.getSnapshotId();
        if ((snapshotId1 != null) && (snapshotId2 != null)) {
            if (snapshotId1.equals(snapshotId2)) {
                return true;
            }
        }

        boolean aHashMatches = false;

        List<String> hash1 = Arrays.asList(hash(object1, schemaComparisons, accordingTo));
        List<String> hash2 = Arrays.asList(hash(object2, schemaComparisons, accordingTo));
        for (String hash : hash1) {
            if (hash2.contains(hash)) {
                aHashMatches = true;
                break;
            }
        }

        if (!aHashMatches) {
            return false;
        }


        return createComparatorChain(object1.getClass(), schemaComparisons, accordingTo).isSameObject(object1, object2, accordingTo);
    }

    public String[] hash(DatabaseObject databaseObject, CompareControl.SchemaComparison[] schemaComparisons, Database accordingTo) {
        String[] hash = null;
        if (databaseObject != null) {
            hash = createComparatorChain(databaseObject.getClass(), schemaComparisons, accordingTo).hash(databaseObject, accordingTo);
        }

        if (hash == null) {
            hash = new String[] {
                    "null"
            };
        }

        for (int i=0; i<hash.length; i++) {
            if (StringUtils.trimToNull(hash[i]) == null) {
                hash[i] = "null";
            }
        }
        return hash;
    }

    public ObjectDifferences findDifferences(DatabaseObject object1, DatabaseObject object2, Database accordingTo, CompareControl compareControl) {
        return createComparatorChain(object1.getClass(), compareControl.getSchemaComparisons(), accordingTo)
            .findDifferences(object1, object2, accordingTo, compareControl, new HashSet<String>());

    }

    private DatabaseObjectComparatorChain createComparatorChain(Class<? extends DatabaseObject> databaseObjectType, CompareControl.SchemaComparison[] schemaComparisons, Database database) {
        String key = databaseObjectType.getName()+":"+database.getShortName();

        if (comparatorChainsByClassAndDatabase.containsKey(key)) {
            DatabaseObjectComparatorChain copy = comparatorChainsByClassAndDatabase.get(key).copy();
            copy.setSchemaComparisons(schemaComparisons);
            return copy;
        }

        List<DatabaseObjectComparator> comparators = DatabaseObjectComparatorFactory.getInstance().getComparators(databaseObjectType, database);
        if ((comparators == null) || comparators.isEmpty()) {
            return null;
        }

        DatabaseObjectComparatorChain chain = new DatabaseObjectComparatorChain(comparators, null);
        comparatorChainsByClassAndDatabase.put(key, chain);
        //noinspection unchecked
        chain = chain.copy();
        chain.setSchemaComparisons(schemaComparisons);
        return chain;
    }

}
