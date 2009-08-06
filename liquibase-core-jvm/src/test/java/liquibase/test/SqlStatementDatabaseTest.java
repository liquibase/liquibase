package liquibase.test;

import liquibase.database.Database;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.exception.DatabaseException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.statement.SqlStatement;

public abstract class SqlStatementDatabaseTest implements DatabaseTest {

    private String schema = null;
    private SqlStatement statement;

    protected SqlStatementDatabaseTest(String schema, SqlStatement statement) {
        this.schema = schema;
        this.statement = statement;
    }

    protected void setup(Database database) throws Exception {
//        try {
//            database.getConnection().commit();
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }

    }


    protected boolean supportsTest(Database database) {
        return true;
    }

    protected boolean expectedException(Database database, DatabaseException exception) {
        if (schema != null && !database.supportsSchemas()) {
            if (exception instanceof StatementNotSupportedOnDatabaseException) {
                return true;
            }
        }
        return false;
    }

    protected abstract void preExecuteAssert(DatabaseSnapshotGenerator snapshotGenerator) throws Exception;

    protected abstract void postExecuteAssert(DatabaseSnapshotGenerator snapshotGenerator) throws Exception;

    public final void performTest(Database database) throws Exception {
//        if (!supportsTest(database)) {
//            return;
//        }
//
//        if (!statement.supportsDatabase(database)) {
//            try {
//                statement.getSqlStatement(database);
//                org.junit.Assert.fail("did not throw exception");
//            } catch (StatementNotSupportedOnDatabaseException e) {
//                return; //what we expected
//            }
//        }
//
//        setup(database);
//        DatabaseSnapshotGenerator snapshot = database.createDatabaseSnapshot(schema, null);
//        preExecuteAssert(snapshot);
//
//        try {
//            new Executor(database).execute(statement);
//            database.getConnection().commit();
//        } catch (DatabaseException e) {
//            boolean expectsException = expectedException(database, e);
//            if (expectsException) {
//                return; //what we wanted
//            } else {
//                throw e;
//            }
//        }
//
//        snapshot = database.createDatabaseSnapshot(schema, null);
//
//        postExecuteAssert(snapshot);
    }
}
