package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;

import java.util.*;

public class ChangeGeneratorFactory {
    private static ChangeGeneratorFactory instance;

    private List<ChangeGenerator> generators = new ArrayList<>();

    private ChangeGeneratorFactory() {
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(ChangeGenerator.class);

            for (Class clazz : classes) {
                register((ChangeGenerator) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Return singleton ChangeGeneratorFactory
     */
    public static synchronized ChangeGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new ChangeGeneratorFactory();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = new ChangeGeneratorFactory();
    }


    public void register(ChangeGenerator generator) {
        generators.add(generator);
    }

    public void unregister(ChangeGenerator generator) {
        generators.remove(generator);
    }

    public void unregister(Class generatorClass) {
        ChangeGenerator toRemove = null;
        for (ChangeGenerator existingGenerator : generators) {
            if (existingGenerator.getClass().equals(generatorClass)) {
                toRemove = existingGenerator;
            }
        }

        unregister(toRemove);
    }

    protected SortedSet<ChangeGenerator> getGenerators(Class<? extends ChangeGenerator> generatorType, Class<? extends DatabaseObject> objectType, Database database) {
        SortedSet<ChangeGenerator> validGenerators = new TreeSet<>(new ChangeGeneratorComparator(objectType, database));

        for (ChangeGenerator generator : generators) {
            if (generatorType.isAssignableFrom(generator.getClass()) && (generator.getPriority(objectType, database)
                > 0)) {
                validGenerators.add(generator);
            }
        }
        return validGenerators;
    }

    private ChangeGeneratorChain createGeneratorChain(Class<? extends ChangeGenerator> generatorType, Class<? extends DatabaseObject> objectType, Database database) {
        SortedSet<ChangeGenerator> generators = getGenerators(generatorType, objectType, database);
        if ((generators == null) || generators.isEmpty()) {
            return null;
        }
        //noinspection unchecked
        return new ChangeGeneratorChain(generators);
    }

    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase) {
        if (!control.shouldOutput(missingObject, comparisionDatabase)) {
            return null;
        }

        ChangeGeneratorChain chain = createGeneratorChain(MissingObjectChangeGenerator.class, missingObject.getClass(), referenceDatabase);
        if (chain == null) {
            return null;
        }
        return chain.fixMissing(missingObject, control, referenceDatabase, comparisionDatabase);
    }

    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase) {
        if (!control.shouldOutput(unexpectedObject, comparisionDatabase)) {
            return null;
        }
        ChangeGeneratorChain chain = createGeneratorChain(UnexpectedObjectChangeGenerator.class, unexpectedObject.getClass(), referenceDatabase);
        if (chain == null) {
            return null;
        }
        return chain.fixUnexpected(unexpectedObject, control, referenceDatabase, comparisionDatabase);
    }

    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase) {
        if (!control.shouldOutput(changedObject, comparisionDatabase)) {
            return null;
        }

        ChangeGeneratorChain chain = createGeneratorChain(ChangedObjectChangeGenerator.class, changedObject.getClass(), referenceDatabase);
        if (chain == null) {
            return null;
        }
        return chain.fixChanged(changedObject, differences, control, referenceDatabase, comparisionDatabase);
    }

    public Set<Class<? extends DatabaseObject>> runAfterTypes(Class<? extends DatabaseObject> objectType, Database database, Class<? extends ChangeGenerator> changeGeneratorType) {
        Set<Class<? extends DatabaseObject>> returnTypes = new HashSet<>();

        SortedSet<ChangeGenerator> generators = getGenerators(changeGeneratorType, objectType, database);

        for (ChangeGenerator generator : generators) {
            Class<? extends DatabaseObject>[] types = generator.runAfterTypes();
            if (types != null) {
                returnTypes.addAll(Arrays.asList(types));
            }
        }
        return returnTypes;
    }

    public Set<Class<? extends DatabaseObject>> runBeforeTypes(Class<? extends DatabaseObject> objectType, Database database, Class<? extends ChangeGenerator> changeGeneratorType) {
        Set<Class<? extends DatabaseObject>> returnTypes = new HashSet<>();

        SortedSet<ChangeGenerator> generators = getGenerators(changeGeneratorType, objectType, database);

        for (ChangeGenerator generator : generators) {
            Class<? extends DatabaseObject>[] types = generator.runBeforeTypes();
            if (types != null) {
                returnTypes.addAll(Arrays.asList(types));
            }
        }
        return returnTypes;
    }


}
