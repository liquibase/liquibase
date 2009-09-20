package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class DatabaseSnapshotGeneratorFactory {

    private static DatabaseSnapshotGeneratorFactory instance;

    private List<DatabaseSnapshotGenerator> registry = new ArrayList<DatabaseSnapshotGenerator>();

    private DatabaseSnapshotGeneratorFactory() {
        try {
            Class[] classes = ServiceLocator.getInstance().findClasses(DatabaseSnapshotGenerator.class);

            for (Class<? extends DatabaseSnapshotGenerator> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static DatabaseSnapshotGeneratorFactory getInstance() {
        if (instance == null) {
             instance = new DatabaseSnapshotGeneratorFactory();
        }
        return instance;
    }

    public DatabaseSnapshotGenerator getGenerator(Database database) {
        return getGenerators(database).iterator().next();
    }


    /**
     * Get generators supporting database, sorted from highest priority to
     * lowest.
     *
     * @param database
     * @return
     */
    public SortedSet<DatabaseSnapshotGenerator> getGenerators(final Database database) {
        SortedSet<DatabaseSnapshotGenerator> generators = new TreeSet<DatabaseSnapshotGenerator>(new Comparator<DatabaseSnapshotGenerator>() {
            public int compare(DatabaseSnapshotGenerator o1, DatabaseSnapshotGenerator o2) {
                return Integer.valueOf(o2.getPriority(database)).compareTo(o1.getPriority(database));
            }
        });

        for (DatabaseSnapshotGenerator generator : registry) {
            if (generator.supports(database)) {
                generators.add(generator);
            }
        }

        return generators;
    }

    /**
     * Get generator for database with highest priority.
     */
    public DatabaseSnapshot createSnapshot(Database database, String schema, Set<DiffStatusListener> listeners) throws DatabaseException {
        return getGenerator(database).createSnapshot(database, schema, listeners);
    }

    /**
     * Returns instances of all implemented database types.
     */
    public List<DatabaseSnapshotGenerator> getRegistry() {
        return registry;
    }

    public void register(DatabaseSnapshotGenerator snapshotGenerator) {
        registry.add(0, snapshotGenerator);
    }

    public static void resetAll() {
        instance = null;
    }
}
