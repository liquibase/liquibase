package liquibase.test;

import liquibase.database.Database;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.snapshot.DatabaseSnapshot;

public abstract class JdbcDatabaseTest implements DatabaseTest {

    protected void setup(Database database) throws Exception {
    }

    protected boolean supportsTest(Database database) {
        return true;
    }

    protected boolean expectedException(Database database, DatabaseException exception) {
        return false;
    }

    protected abstract void executeStatements(Database database) throws Exception;

    protected abstract void preExecuteAssert(DatabaseSnapshot snapshot) throws Exception;

    protected abstract void postExecuteAssert(DatabaseSnapshot snapshot) throws Exception;

    public final void performTest(Database database) throws Exception {
        if (!supportsTest(database)) {
            return;
        }

        setup(database);
        DatabaseSnapshot snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);
        preExecuteAssert(snapshot);

        try {
            executeStatements(database);
            database.getConnection().commit();
        } catch (DatabaseException e) {
            boolean expectsException = expectedException(database, e);
            if (expectsException) {
                return; //what we wanted
            } else {
                throw e;
            }
        }

        snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);

        postExecuteAssert(snapshot);
    }

}
