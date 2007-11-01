package liquibase.database.sql;

import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AlterSequenceStatementTest extends AbstractSqlStatementTest {

    private static final String SEQ_NAME = "altertest_seq".toUpperCase();

    @Before
    public void dropAndCreateSequence() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            if (database.supportsSequences()) {
                dropAndCreateSequence(new CreateSequenceStatement(null, SEQ_NAME), database);

                if (database.supportsSchemas()) {
                    dropAndCreateSequence(new CreateSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME), database);
                }
            }
        }
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

                AlterSequenceStatement statement = new AlterSequenceStatement(null, SEQ_NAME).setIncrementBy(5);

                if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert increment by is 1

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

                AlterSequenceStatement statement = new AlterSequenceStatement(null, SEQ_NAME).setMinValue(0);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo; assert minValue is 1

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

                AlterSequenceStatement statement = new AlterSequenceStatement(null, SEQ_NAME).setMaxValue(50);

                if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert initial max value

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

                AlterSequenceStatement statement = new AlterSequenceStatement(null, SEQ_NAME).setOrdered(true);

                if (!(database instanceof OracleDatabase || database instanceof DB2Database)) {
                    try {
                        statement.getSqlStatement(database);
                        fail("Should have thrown exception");
                    } catch (StatementNotSupportedOnDatabaseException e) {
                        return; //what we wanted
                    }
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert order default

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getSequence(SEQ_NAME));
                //todo: assert max value
            }
        });
    }

}
