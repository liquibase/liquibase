package liquibase.diff.compare;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class DatabaseObjectComparatorFactory {

    private static DatabaseObjectComparatorFactory instance;

    private List<DatabaseObjectComparator> comparators = new ArrayList<DatabaseObjectComparator>();

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

    protected SortedSet<DatabaseObjectComparator> getComparators(Class<? extends DatabaseObject> comparatorClass, Database database) {
        SortedSet<DatabaseObjectComparator> validComparators = new TreeSet<DatabaseObjectComparator>(new DatabaseObjectComparatorComparator(comparatorClass, database));

        for (DatabaseObjectComparator comparator : comparators) {
            if (comparator.getPriority(comparatorClass, database) > 0) {
                validComparators.add(comparator);
            }
        }
        return validComparators;
    }


    public static void resetAll() {
        instance = null;
    }

    public boolean isSameObject(DatabaseObject object1, DatabaseObject object2, Database accordingTo) {
        return createComparatorChain(object1.getClass(), accordingTo).isSameObject(object1, object2, accordingTo);
    }

    public ObjectDifferences findDifferences(DatabaseObject object1, DatabaseObject object2, Database accordingTo) {
        return createComparatorChain(object1.getClass(), accordingTo).findDifferences(object1, object2, accordingTo);

    }

    private DatabaseObjectComparatorChain createComparatorChain(Class<? extends DatabaseObject> databaseObjectType, Database database) {
        SortedSet<DatabaseObjectComparator> comparators = DatabaseObjectComparatorFactory.getInstance().getComparators(databaseObjectType, database);
        if (comparators == null || comparators.size() == 0) {
            return null;
        }
        //noinspection unchecked
        return new DatabaseObjectComparatorChain(comparators);
    }

}
