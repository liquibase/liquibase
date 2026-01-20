package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

public class RestoredDatabaseSnapshot extends DatabaseSnapshot {

    public RestoredDatabaseSnapshot(Database database) throws DatabaseException, InvalidExampleException {
        super(new DatabaseObject[0], database);
    }

    public RestoredDatabaseSnapshot(Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        super(new DatabaseObject[0], database, snapshotControl);
    }
}
