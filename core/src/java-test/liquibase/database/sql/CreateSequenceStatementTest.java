package liquibase.database.sql;

import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CreateSequenceStatementTest extends AbstractSqlStatementTest {

    private static final String SEQ_NAME = "createtest_seq".toUpperCase();

    @Before
    public void dropSequence() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            if (database.supportsSequences()) {
                dropSequenceIfExists(null, SEQ_NAME, database);

                if (database.supportsSchemas()) {
                    dropSequenceIfExists(TestContext.ALT_SCHEMA, SEQ_NAME, database);
                }
            }
        }
    }

    protected SqlStatement generateTestStatement() {
        return new CreateSequenceStatement(null, null);
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (database.supportsSequences()) {
                    assertTrue(generateTestStatement().supportsDatabase(database));
                } else {
                    assertFalse(generateTestStatement().supportsDatabase(database));
                }
            }
        });
    }

    @Test
    public void execute() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences()) {
                    try {
                        generateTestStatement().getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(new CreateSequenceStatement(null, SEQ_NAME));

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
            }
        });
    }

    @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences() || !database.supportsSchemas()) {
                    return;
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(new CreateSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME));

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
            }
        });
    }

    @Test
    public void execute_startValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences()) {
                    try {
                        generateTestStatement().getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                CreateSequenceStatement statement = new CreateSequenceStatement(null, SEQ_NAME).setStartValue(1000);

                if (database instanceof FirebirdDatabase) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert start value
            }
        });
    }

    @Test
    public void execute_incrementBy() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences()) {
                    try {
                        generateTestStatement().getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                CreateSequenceStatement statement = new CreateSequenceStatement(null, SEQ_NAME).setIncrementBy(5);

                if (database instanceof FirebirdDatabase) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert increment by value
            }
        });
    }

    @Test
    public void execute_minValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences()) {
                    try {
                        generateTestStatement().getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                CreateSequenceStatement statement = new CreateSequenceStatement(null, SEQ_NAME).setMinValue(15);

                if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert min valuevalue
            }
        });
    }

    @Test
    public void execute_maxValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences()) {
                    try {
                        generateTestStatement().getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                CreateSequenceStatement statement = new CreateSequenceStatement(null, SEQ_NAME).setMaxValue(50);

                if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert max value
            }
        });
    }

    @Test
    public void execute_order() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsSequences()) {
                    try {
                        generateTestStatement().getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                CreateSequenceStatement statement = new CreateSequenceStatement(null, SEQ_NAME).setOrdered(true);

                if (!(database instanceof OracleDatabase || database instanceof DB2Database)) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert max value
            }
        });
    }
}
