package liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

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

    /**
     * This calls all the non-replaced {@link SnapshotGenerator} in the chain, by comparison order
     * Only the first generator in the chain is allowed to create a new instance of T
     * Subsequent generators must modify the instance or call the chain if the provided object is not handled,
     * otherwise a {@link DatabaseException} is thrown
     *
     * @return snapshot object
     * @throws DatabaseException if any of the subsequent generators return an instance different from the first generator's
     *                           invocation result
     * @see SnapshotGenerator#replaces() to skip generators that do not comply to the above requireemnts
     */
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

        boolean firstRoundDone = false;
        SnapshotGenerator lastGenerator = null;
        T lastObject = example;
        while (snapshotGenerators.hasNext()) {
            SnapshotGenerator generator = snapshotGenerators.next();
            if (replacedGenerators.contains(generator.getClass())) {
                continue;
            }
            T object = generator.snapshot(lastObject, snapshot, this);
            if ((object != null) && (object.getSnapshotId() == null)) {
                object.setSnapshotId(snapshotIdService.generateId());
            }
            // only first generator in the chain is allowed to create new instances - subsequent ones are not
            if (firstRoundDone && object != lastObject) {
                throw new DatabaseException(String.format("Snapshot generator %s has returned a different reference from the previous generator %s.\n" +
                                "\tSnapshot object was: %s, it is now: %s.\n" +
                                "\tConsider declaring %1$s as being replaced by one the generator in the chain via liquibase.snapshot.SnapshotGenerator#replaces.",
                        generator.getClass().getName(),
                        lastGenerator.getClass().getName(),
                        identity(lastObject),
                        identity(object)));
            }
            lastObject = object;
            lastGenerator = generator;
            firstRoundDone = true;
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
