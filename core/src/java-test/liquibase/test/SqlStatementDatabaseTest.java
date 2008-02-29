package liquibase.test;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public abstract class SqlStatementDatabaseTest implements DatabaseTest {

    private String schema = null;
    private SqlStatement statement;

    protected SqlStatementDatabaseTest(String schema, SqlStatement statement) {
        this.schema = schema;
        this.statement = statement;
    }

    protected void setup(Database database) throws JDBCException {
//        try {
//            database.getConnection().commit();
//        } catch (SQLException e) {
//            throw new JDBCException(e);
//        }

    }


    protected boolean supportsTest(Database database) {
        return true;
    }

    protected boolean expectedException(Database database, JDBCException exception) {
        if (schema != null && !database.supportsSchemas()) {
            if (exception instanceof StatementNotSupportedOnDatabaseException) {
                return true;
            }
        }
        return false;
    }

    protected abstract void preExecuteAssert(DatabaseSnapshot snapshot) throws Exception;

    protected abstract void postExecuteAssert(DatabaseSnapshot snapshot) throws Exception;

    public final void performTest(Database database) throws Exception {
        if (!supportsTest(database)) {
            return;
        }

        if (!statement.supportsDatabase(database)) {
            try {
                statement.getSqlStatement(database);
                org.junit.Assert.fail("did not throw exception");
            } catch (StatementNotSupportedOnDatabaseException e) {
                return; //what we expected
            }
        }

        setup(database);
        DatabaseSnapshot snapshot = database.createDatabaseSnapshot(schema, null);
        preExecuteAssert(snapshot);

        try {
            new JdbcTemplate(database).execute(statement);
            database.getConnection().commit();
        } catch (JDBCException e) {
            boolean expectsException = expectedException(database, e);
            if (expectsException) {
                return; //what we wanted
            } else {
                throw e;
            }
        }

        snapshot = database.createDatabaseSnapshot(schema, null);

        postExecuteAssert(snapshot);
    }
}
