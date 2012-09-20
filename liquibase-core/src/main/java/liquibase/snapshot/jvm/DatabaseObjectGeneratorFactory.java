package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

public class DatabaseObjectGeneratorFactory {

    private static DatabaseObjectGeneratorFactory instance;

    private List<DatabaseObjectSnapshotGenerator> generators = new ArrayList<DatabaseObjectSnapshotGenerator>();

    private DatabaseObjectGeneratorFactory() {
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(DatabaseObjectSnapshotGenerator.class);

            for (Class clazz : classes) {
                register((DatabaseObjectSnapshotGenerator) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Return singleton DatabaseObjectGeneratorFactory
     */
    public static DatabaseObjectGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseObjectGeneratorFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new DatabaseObjectGeneratorFactory();
    }


    public void register(DatabaseObjectSnapshotGenerator generator) {
        generators.add(generator);
    }

    public void unregister(DatabaseObjectSnapshotGenerator generator) {
        generators.remove(generator);
    }

    public void unregister(Class generatorClass) {
        DatabaseObjectSnapshotGenerator toRemove = null;
        for (DatabaseObjectSnapshotGenerator existingGenerator : generators) {
            if (existingGenerator.getClass().equals(generatorClass)) {
                toRemove = existingGenerator;
            }
        }

        unregister(toRemove);
    }

    public <T extends DatabaseObject> DatabaseObjectSnapshotGenerator<T> getGenerator(Class<T> generatorClass, Database database) {
        SortedSet<DatabaseObjectSnapshotGenerator> generators = getGenerators(generatorClass, database);
        if (generators != null && generators.size() > 0) {
            return generators.iterator().next();
        }
        return null;
    }

    protected Collection<DatabaseObjectSnapshotGenerator> getGenerators() {
        return generators;
    }

    protected SortedSet<DatabaseObjectSnapshotGenerator> getGenerators(Class<? extends DatabaseObject> generatorClass, Database database) {
        SortedSet<DatabaseObjectSnapshotGenerator> validGenerators = new TreeSet<DatabaseObjectSnapshotGenerator>(new DatabaseObjectGeneratorComparator());

        for (DatabaseObjectSnapshotGenerator generator : getGenerators()) {
            Class clazz = generator.getClass();
            Type classType = null;
            while (clazz != null) {
                if (classType instanceof ParameterizedType) {
                    checkType(classType, generatorClass, generator, database, validGenerators);
                }

                for (Type type : clazz.getGenericInterfaces()) {
                    if (type instanceof ParameterizedType) {
                        checkType(type, generatorClass, generator, database, validGenerators);
                    } else if (isTypeEqual(type, DatabaseObjectSnapshotGenerator.class)) {
                        //noinspection unchecked
                        if (generator.supports(generatorClass, database)) {
                            validGenerators.add(generator);
                        }
                    }
                }
                classType = clazz.getGenericSuperclass();
                clazz = clazz.getSuperclass();
            }
        }
        return validGenerators;
    }

    private boolean isTypeEqual(Type aType, Class aClass) {
        if (aType instanceof Class) {
            return ((Class) aType).getName().equals(aClass.getName());
        }
        return aType.equals(aClass);
    }

    private void checkType(Type type, Class<? extends DatabaseObject> databaseObject, DatabaseObjectSnapshotGenerator generator, Database database, SortedSet<DatabaseObjectSnapshotGenerator> validGenerators) {
        for (Type typeClass : ((ParameterizedType) type).getActualTypeArguments()) {
            if (typeClass instanceof TypeVariable) {
                typeClass = ((TypeVariable) typeClass).getBounds()[0];
            }

            if (isTypeEqual(typeClass, DatabaseObject.class)) {
                return;
            }

            if (((Class) typeClass).isAssignableFrom(databaseObject)) {
                if (generator.supports(databaseObject, database)) {
                    validGenerators.add(generator);
                }
            }
        }

    }


}
