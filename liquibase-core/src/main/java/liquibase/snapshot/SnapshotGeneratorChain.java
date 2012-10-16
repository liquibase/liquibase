package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;

import java.util.Iterator;
import java.util.SortedSet;

public class SnapshotGeneratorChain {
    private Iterator<SnapshotGenerator> snapshotGenerators;

    public SnapshotGeneratorChain(SortedSet<SnapshotGenerator> snapshotGenerators) {
        if (snapshotGenerators != null) {
            this.snapshotGenerators = snapshotGenerators.iterator();
        }
    }

    public DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (example == null) {
            return null;
        }

        if (!(example instanceof Catalog || example instanceof Schema) && !snapshot.getSnapshotControl().shouldSnapshot(example.getClass())) {
            return null;
        }

        if (snapshotGenerators == null) {
            return null;
        }

        if (!snapshotGenerators.hasNext()) {
            return null;
        }

        return snapshotGenerators.next().snapshot(example, snapshot, this);
    }

//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
//        if (snapshotGenerators == null) {
//            return null;
//        }
//
//        if (!snapshotGenerators.hasNext()) {
//            return null;
//        }
//
//        return snapshotGenerators.next().has(example, snapshot, this);
//    }
}
