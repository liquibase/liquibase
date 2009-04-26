package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.ReorganizeTableStatement;
import liquibase.database.statement.AbstractSqStatementTest;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import org.junit.Test;

public class ReorganizeTableGeneratorTest extends AbstractSqStatementTest {
    private static final String TABLE_NAME = "AddReorgTableTest";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME).addColumn("existingCol", "int"), database);
        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME).addColumn("existingCol", "int"), database);
    }

    protected SqlStatement createGeneratorUnderTest() {
        return new ReorganizeTableStatement(null, null);
    }

//    @Test
//    public void isValidGenerator() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                if (database instanceof DB2Database) {
//                    assertTrue(new ReorganizeTableStatement(null, null).supportsDatabase(database));
//                } else {
//                    assertFalse(new ReorganizeTableStatement(null, null).supportsDatabase(database));
//                }
//            }
//        });
//    }

    @Test
    public void execute_noSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new ReorganizeTableStatement(null, TABLE_NAME)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        ; //nothing to test
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        ; //nothing to test
                    }
                });
    }

    @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new ReorganizeTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        ; //nothing to test
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        ; //nothing to test
                    }
                });
    }
}
