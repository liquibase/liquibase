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
     * This calls all the non-replaced {@link SnapshotGenerator} in the chain, by comparison order.
     * <p>
     * During the chain processing, the snapshot method returns an object that will be passed to the next generator in the chain.
     * The object can be the same object that was passed to the generator, or a new object, given that:
     * - the generator copies the attributes of the provided object to a new object and returns the new object (liquibase-hibernate extension does this in https://github.com/liquibase/liquibase-hibernate/blob/main/src/main/java/liquibase/ext/hibernate/snapshot/IndexSnapshotGenerator.java#L31 )
     * - the generator returns the existing instance (they can e.g. set new attributes to it)
     * <p>
     * Snapshot generators that do not abide by the previous rules must be the first to be called for a given object type and there are not more than one of these per object type.
     * Note that this can get tricky as extensions can bring their own snapshot generators to the mix breaking core generators.
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
