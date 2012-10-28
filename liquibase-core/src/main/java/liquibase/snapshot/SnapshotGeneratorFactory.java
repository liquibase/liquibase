package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

public class SnapshotGeneratorFactory {

    private static SnapshotGeneratorFactory instance;

    private List<SnapshotGenerator> generators = new ArrayList<SnapshotGenerator>();

    private SnapshotGeneratorFactory() {
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(SnapshotGenerator.class);

            for (Class clazz : classes) {
                register((SnapshotGenerator) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Return singleton SnapshotGeneratorFactory
     */
    public static SnapshotGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new SnapshotGeneratorFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new SnapshotGeneratorFactory();
    }


    public void register(SnapshotGenerator generator) {
        generators.add(generator);
    }

    public void unregister(SnapshotGenerator generator) {
        generators.remove(generator);
    }

    public void unregister(Class generatorClass) {
        SnapshotGenerator toRemove = null;
        for (SnapshotGenerator existingGenerator : generators) {
            if (existingGenerator.getClass().equals(generatorClass)) {
                toRemove = existingGenerator;
            }
        }

        unregister(toRemove);
    }

    public boolean has(DatabaseObject example, Database database) throws DatabaseException, InvalidExampleException {
        return createSnapshot(example, database) != null;
    }

    public Collection<SnapshotGenerator> getGenerators() {
        return generators;
    }

    protected SortedSet<SnapshotGenerator> getGenerators(Class<? extends DatabaseObject> generatorClass, Database database) {
        SortedSet<SnapshotGenerator> validGenerators = new TreeSet<SnapshotGenerator>(new SnapshotGeneratorComparator(generatorClass, database));

        for (SnapshotGenerator generator : getGenerators()) {
            Class clazz = generator.getClass();
            Type classType = null;
            while (clazz != null) {
                if (classType instanceof ParameterizedType) {
                    checkType(classType, generatorClass, generator, database, validGenerators);
                }

                for (Type type : clazz.getGenericInterfaces()) {
                    if (type instanceof ParameterizedType) {
                        checkType(type, generatorClass, generator, database, validGenerators);
                    } else if (isTypeEqual(type, SnapshotGenerator.class)) {
                        //noinspection unchecked
                        if (generator.getPriority(generatorClass, database) > 0) {
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

    private void checkType(Type type, Class<? extends DatabaseObject> databaseObject, SnapshotGenerator generator, Database database, SortedSet<SnapshotGenerator> validGenerators) {
        for (Type typeClass : ((ParameterizedType) type).getActualTypeArguments()) {
            if (typeClass instanceof TypeVariable) {
                typeClass = ((TypeVariable) typeClass).getBounds()[0];
            }

            if (isTypeEqual(typeClass, DatabaseObject.class)) {
                return;
            }

            if (((Class) typeClass).isAssignableFrom(databaseObject)) {
                if (generator.getPriority(databaseObject, database) > 0) {
                    validGenerators.add(generator);
                }
            }
        }

    }


    public DatabaseSnapshot createSnapshot(SnapshotControl snapshotControl, Database database, DatabaseObject... examples) throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot snapshot = new DatabaseSnapshot(snapshotControl, database);

        for (DatabaseObject example : examples) {
            snapshot.include(example);
        }

        return snapshot;
    }

    public <T extends DatabaseObject> T createSnapshot(T example, Database database) throws DatabaseException, InvalidExampleException {
            DatabaseSnapshot snapshot = createSnapshot(new SnapshotControl(example.getClass()), database, example);
            return snapshot.get(example);
    }

    public <T extends DatabaseObject> T createSnapshot(T example, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        DatabaseSnapshot snapshot = createSnapshot(snapshotControl, database, example);
        return snapshot.get(example);
    }

    public Table getDatabaseChangeLogTable(SnapshotControl snapshotControl, Database database) throws DatabaseException {
        try {
            Table liquibaseTable = (Table) new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()));
            return createSnapshot(liquibaseTable, database, snapshotControl);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        try {
            Table example = (Table) new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()));
            return createSnapshot(example, database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean hasDatabaseChangeLogTable(Database database) throws DatabaseException {
        try {
            return has(new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean hasDatabaseChangeLogLockTable(Database database) throws DatabaseException {
        try {
            return has(new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())), database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public static void resetAll() {
        instance = null;
    }
}
