package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

public interface SnapshotGenerator {

    final int PRIORITY_NONE = -1;
    final int PRIORITY_DEFAULT = 1;
    final int PRIORITY_DATABASE = 5;
    final int PRIORITY_ADDITIONAL = 50;

    int getPriority(Class<? extends DatabaseObject> objectType, Database database);

    <T extends DatabaseObject> T snapshot(T example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException;

    Class<? extends DatabaseObject>[] addsTo();

    /**
     * Returns classes (and superclasses) that this SnapshotGenerator replaces. Return null or empty array to not affect the SnapshotGeneratorChain.
     */
    Class<? extends SnapshotGenerator>[] replaces();
}
