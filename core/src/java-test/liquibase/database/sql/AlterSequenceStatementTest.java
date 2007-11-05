package liquibase.database.sql;

import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AlterSequenceStatementTest extends AbstractSqlStatementTest {

    private static final String SEQ_NAME = "altertest_seq".toUpperCase();

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateSequence(new CreateSequenceStatement(null, SEQ_NAME), database);
        dropAndCreateSequence(new CreateSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME), database);
    }

    protected SqlStatement generateTestStatement() {
        return new AlterSequenceStatement(null, null);
    }

    @Test
    public void supportsDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database.supportsSequences()) {
                assertTrue(generateTestStatement().supportsDatabase(database));
            } else {
                assertFalse(generateTestStatement().supportsDatabase(database));
            }
        }
    }

    @Test
    public void execute_incrementBy() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setIncrementBy(5)) {
                    protected boolean expectedException(Database database, JDBCException exception) {
                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase;
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert increment by is 1
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert increment by value
                    }
                });
    }

    @Test
    public void execute_minValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setMinValue(0)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo; assert minValue is 1
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert min valuevalue
                    }
                });
    }

    @Test
    public void execute_maxValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setMaxValue(50)) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase;
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert initial max value
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert max value
                    }
                });
    }

    @Test
    public void execute_order() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setOrdered(true)) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return !(database instanceof OracleDatabase || database instanceof DB2Database);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert order default
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert max value
                    }
                });
    }

    @Test
    public void execute_schemaSet() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AlterSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME).setIncrementBy(5)) {
                    protected boolean expectedException(Database database, JDBCException exception) {
                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase;
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert increment by is 1
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert increment by value
                    }
                });
    }

}
