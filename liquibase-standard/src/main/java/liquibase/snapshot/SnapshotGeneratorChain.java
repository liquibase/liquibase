package liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;

public class SnapshotGeneratorChain {
    private Iterator<SnapshotGenerator> snapshotGenerators;

    private final Set<Class<? extends SnapshotGenerator>> replacedGenerators = new HashSet<>();
    private final SnapshotIdService snapshotIdService;

    public SnapshotGeneratorChain(SortedSet<SnapshotGenerator> snapshotGenerators) {
        snapshotIdService = SnapshotIdService.getInstance();

        if (snapshotGenerators != null) {
            this.snapshotGenerators = snapshotGenerators.iterator();

            for (SnapshotGenerator generator : snapshotGenerators) {
                Class<? extends SnapshotGenerator>[] replaces = generator.replaces();
                if ((replaces != null) && (replaces.length > 0)) {
                    replacedGenerators.addAll(Arrays.asList(replaces));
                }
            }
        }
    }

    public <T extends DatabaseObject> T snapshot(T example, DatabaseSnapshot snapshot)
            throws DatabaseException, InvalidExampleException {
        if (example == null) {
            return null;
        }

        if (snapshot.getDatabase().isSystemObject(example)) {
            return null;
        }

        if (!snapshot.getSnapshotControl().shouldInclude(example.getClass())) {
            return null;
        }

        if (snapshotGenerators == null) {
            return null;
        }

        T lastObject = null;
        SnapshotGenerator lastGenerator = null;
        boolean resultInitialized = false;
        while (snapshotGenerators.hasNext()) {
            SnapshotGenerator generator = snapshotGenerators.next();
            if (replacedGenerators.contains(generator.getClass())) {
                continue;
            }
            T object = generator.snapshot(example, snapshot, this);
            if ((object != null) && (object.getSnapshotId() == null)) {
                object.setSnapshotId(snapshotIdService.generateId());
            }
            if (resultInitialized && lastObject != object) {
                throw new DatabaseException(String.format("Snapshot generator %s has returned a different reference from the previous generator %s.\n" +
                                                          "\tSnapshot object was: %s, it is now: %s.\n" +
                                                          "\tConsider adding one of the generator to the result of the other's implementation of liquibase.snapshot.SnapshotGenerator#replaces.",
                        generator.getClass().getName(),
                        lastGenerator.getClass().getName(),
                        identity(lastObject),
                        identity(object)));
            }
            lastObject = object;
            lastGenerator = generator;
            resultInitialized = true;
        }
        return lastObject;
    }

    private static String identity(Object object) {
        if (object == null) {
            return "null";
        }
        return String.format("%s@%s", object.getClass(), System.identityHashCode(object));
    }
}
