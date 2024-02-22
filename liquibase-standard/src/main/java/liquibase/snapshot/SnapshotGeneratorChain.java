package liquibase.snapshot;

import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

import java.util.*;
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

        T result = null;
        boolean resultInitialized = false;
        while (snapshotGenerators.hasNext()) {
            SnapshotGenerator generator = snapshotGenerators.next();
            if (replacedGenerators.stream()
                    .anyMatch(Predicate.isEqual(generator.getClass()))) {
                continue;
            }
            T object = generator.snapshot(example, snapshot, this);
            if ((object != null) && (object.getSnapshotId() == null)) {
                object.setSnapshotId(snapshotIdService.generateId());
            }
            if (resultInitialized && result != object) {
                Scope.getCurrentScope().getLog(getClass())
                        .warning(String.format("Snapshot generator %s has returned a different reference." +
                                 "Main snapshot object was: %s, it is now: %s",
                                generator.getClass().getName(), result, object));
            }
            result = object;
            resultInitialized = true;
        }
        return result;
    }
}
