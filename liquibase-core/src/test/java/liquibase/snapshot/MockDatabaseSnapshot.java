package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class MockDatabaseSnapshot extends DatabaseSnapshot {

    private final List<DatabaseObject> configuredObjects;

    public MockDatabaseSnapshot(List<DatabaseObject> configuredObjects, DatabaseObject[] examples, Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        super(null, database, snapshotControl);
        this.configuredObjects = configuredObjects;
        init(examples);
    }

    @Override
    protected SnapshotGeneratorChain createGeneratorChain(Class<? extends DatabaseObject> databaseObjectType, Database database) {
        return new SnapshotGeneratorChain(new TreeSet<SnapshotGenerator>(Arrays.asList(new MockSnapshotGenerator())));
    }

    private class MockSnapshotGenerator implements SnapshotGenerator, Comparable<SnapshotGenerator> {

        @Override
        public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
            return PRIORITY_DEFAULT;
        }

        @Override
        public <T extends DatabaseObject> T snapshot(T example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
            for (DatabaseObject object : configuredObjects) {
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, example, null, MockDatabaseSnapshot.this.getDatabase())) {
                    return (T) object;
                }
            }
            return null;
        }

        @Override
        public int compareTo(SnapshotGenerator o) {
            return this.toString().compareTo(o.toString());
        }

        @Override
        public Class<? extends DatabaseObject>[] addsTo() {
            return null;
        }

        @Override
        public Class<? extends SnapshotGenerator>[] replaces() {
            return null;
        }
    }
}
