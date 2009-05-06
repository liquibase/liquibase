package liquibase.statement;

import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class CreateSequenceGeneratorTest extends AbstractSqStatementTest {

    private static final String SEQ_NAME = "createtest_seq".toUpperCase();

    protected void setupDatabase(Database database) throws Exception {
        dropSequenceIfExists(null, SEQ_NAME, database);

        dropSequenceIfExists(TestContext.ALT_SCHEMA, SEQ_NAME, database);
    }

    protected SqlStatement createGeneratorUnderTest() {
        return new CreateSequenceStatement(null, null);
    }

//    @Test
//    public void isValidGenerator() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                if (database.supportsSequences()) {
//                    assertTrue(createGeneratorUnderTest().supportsDatabase(database));
//                } else {
//                    assertFalse(createGeneratorUnderTest().supportsDatabase(database));
//                }
//            }
//        });
//    }

    @Test
    public void execute() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getSequence(SEQ_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                    }
                });
    }

    @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getSequence(SEQ_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                    }
                });
    }

    @Test
    public void execute_startValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setStartValue(1000)) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return database instanceof FirebirdDatabase;
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getSequence(SEQ_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert start value
                    }
                });
    }

    @Test
    public void execute_incrementBy() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setIncrementBy(5)) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return database instanceof FirebirdDatabase;
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getSequence(SEQ_NAME));
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
                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setMinValue(15)) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase;
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getSequence(SEQ_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                        //todo: assert min valuevalue
                    }
                });
    }

    @Test
    public void execute_maxValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setMaxValue(50)) {

            protected boolean expectedException(Database database, JDBCException exception) {
                return database instanceof FirebirdDatabase || database instanceof HsqlDatabase;
            }

            protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                assertNull(snapshot.getSequence(SEQ_NAME));
            }

            protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert max value
            }
        });
    }

    @Test
    public void execute_order() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setOrdered(true)) {
            protected boolean expectedException(Database database, JDBCException exception) {
                return !(database instanceof OracleDatabase || database instanceof DB2Database);
            }

            protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                assertNull(snapshot.getSequence(SEQ_NAME));
            }

            protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert max value
            }
        });
    }
}
