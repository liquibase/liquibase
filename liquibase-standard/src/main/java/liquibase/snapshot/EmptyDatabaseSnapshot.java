package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

public class EmptyDatabaseSnapshot extends DatabaseSnapshot {
    public EmptyDatabaseSnapshot(Database database) throws DatabaseException, InvalidExampleException {
        super(new DatabaseObject[0], database);
    }

    public EmptyDatabaseSnapshot(Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        super(new DatabaseObject[0], database, snapshotControl);
    }
}
