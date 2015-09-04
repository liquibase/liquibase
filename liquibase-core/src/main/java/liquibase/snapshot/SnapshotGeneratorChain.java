package liquibase.snapshot;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.UUID;

import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

public class SnapshotGeneratorChain {
    private Iterator<SnapshotGenerator> snapshotGenerators;

    public SnapshotGeneratorChain(SortedSet<SnapshotGenerator> snapshotGenerators) {
        if (snapshotGenerators != null) {
            this.snapshotGenerators = snapshotGenerators.iterator();
        }
    }

    public <T extends DatabaseObject> T snapshot(T example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
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

        if (!snapshotGenerators.hasNext()) {
            return null;
        }

        T obj = snapshotGenerators.next().snapshot(example, snapshot, this);
        if (obj != null && obj.getSnapshotId() == null) {
            obj.setSnapshotId(UUID.randomUUID());
        }
        return obj;
    }
}
