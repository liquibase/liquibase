package liquibase.test;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

import java.sql.SQLException;
import static junit.framework.Assert.*;

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
        return false;
    }

    protected abstract void preExecuteAssert(DatabaseSnapshot snapshot);

    protected abstract void postExecuteAssert(DatabaseSnapshot snapshot);

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
        DatabaseSnapshot snapshot = new DatabaseSnapshot(database, schema);
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

        snapshot = new DatabaseSnapshot(database, schema);

        postExecuteAssert(snapshot);
    }
}
