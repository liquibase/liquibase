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
     *
     * @return snapshot object
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

        T objectToSnapshot = example;
        while (snapshotGenerators.hasNext()) {
            SnapshotGenerator generator = snapshotGenerators.next();
            if (replacedGenerators.contains(generator.getClass())) {
                continue;
            }
            T object = generator.snapshot(objectToSnapshot, snapshot, this);
            if ((object != null) && (object.getSnapshotId() == null)) {
                object.setSnapshotId(snapshotIdService.generateId());
            }
            objectToSnapshot = object;
        }
        return objectToSnapshot;
    }
}
