package liquibase.snapshot;

import liquibase.ExecutionEnvironment;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

public class EmptyDatabaseSnapshot extends NewDatabaseSnapshot {
    public EmptyDatabaseSnapshot(Database database) throws DatabaseException, InvalidExampleException {
        super(new SnapshotControl(database), new ExecutionEnvironment(database));
    }

    public EmptyDatabaseSnapshot(Database database, SnapshotControl snapshotControl) throws DatabaseException, InvalidExampleException {
        super(snapshotControl, new ExecutionEnvironment(database));
    }
}
