package liquibase.test;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;

public abstract class JdbcDatabaseTest implements DatabaseTest {

    protected void setup(Database database) throws Exception {
    }

    protected boolean supportsTest(Database database) {
        return true;
    }

    protected boolean expectedException(Database database, JDBCException exception) {
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
        DatabaseSnapshot snapshot = database.createDatabaseSnapshot(null, null);
        preExecuteAssert(snapshot);

        try {
            executeStatements(database);
            database.getConnection().commit();
        } catch (JDBCException e) {
            boolean expectsException = expectedException(database, e);
            if (expectsException) {
                return; //what we wanted
            } else {
                throw e;
            }
        }

        snapshot = database.createDatabaseSnapshot(null, null);

        postExecuteAssert(snapshot);
    }

}
