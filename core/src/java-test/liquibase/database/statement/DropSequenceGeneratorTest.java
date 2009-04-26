package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.DropSequenceStatement;
import liquibase.database.statement.CreateSequenceStatement;
import liquibase.database.statement.AbstractSqStatementTest;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropSequenceGeneratorTest extends AbstractSqStatementTest {

    protected SqlStatement createGeneratorUnderTest() {
        return new DropSequenceStatement(null, null);
    }

    private static final String SEQ_NAME = "DROPTEST_SEQ";

    protected void setupDatabase(Database database) throws Exception {
                dropAndCreateSequence(new CreateSequenceStatement(null, SEQ_NAME), database);
                dropAndCreateSequence(new CreateSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME), database);
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
    public void execute_noSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropSequenceStatement(null, SEQ_NAME)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getSequence(SEQ_NAME));
                    }
                });
    }

    @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getSequence(SEQ_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getSequence(SEQ_NAME));
                    }
                });
    }
}
