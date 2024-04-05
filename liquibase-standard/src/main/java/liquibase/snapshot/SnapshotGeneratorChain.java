package liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

import java.util.*;

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

        SnapshotGenerator next = getNextValidGenerator();

        if (next == null) {
            return null;
        }

        T obj = next.snapshot(example, snapshot, this);
        if ((obj != null) && (obj.getSnapshotId() == null)) {
            obj.setSnapshotId(snapshotIdService.generateId());
        }
        return obj;
    }

    public SnapshotGenerator getNextValidGenerator() {
        if (snapshotGenerators == null) {
            return null;
        }

        if (!snapshotGenerators.hasNext()) {
            return null;
        }

        SnapshotGenerator next = snapshotGenerators.next();
        for (Class<? extends SnapshotGenerator> removedGenerator : replacedGenerators) {
            if (removedGenerator.equals(next.getClass())) {
                return getNextValidGenerator();
            }
        }
        return next;
    }
}
