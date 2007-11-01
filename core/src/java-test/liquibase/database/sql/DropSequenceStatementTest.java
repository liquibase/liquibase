package liquibase.database.sql;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import liquibase.database.Database;
import liquibase.database.template.JdbcTemplate;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.DatabaseTest;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropSequenceStatementTest extends AbstractSqlStatementTest {

    protected SqlStatement generateTestStatement() {
        return new DropSequenceStatement(null, null);
    }

    private static final String SEQ_NAME = "DROPTEST_SEQ";

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
    public void execute_noSchema() throws Exception {
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
                assertNotNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(new DropSequenceStatement(null, SEQ_NAME));

                snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getSequence(SEQ_NAME));
            }
        });
    }

    @Test
    public void execute_withSchema() throws Exception {
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

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNotNull(snapshot.getSequence(SEQ_NAME));

                new JdbcTemplate(database).execute(new DropSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME));

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNull(snapshot.getSequence(SEQ_NAME));
            }
        });
    }
}
