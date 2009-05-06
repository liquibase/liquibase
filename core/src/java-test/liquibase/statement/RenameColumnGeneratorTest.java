package liquibase.statement;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class RenameColumnGeneratorTest extends AbstractSqStatementTest {
    private static final String TABLE_NAME = "RenameColumnTest";
    private static final String COL_NAME = "testCol";
    private static final String NEW_COL_NAME = "newColName";
    private static final String DATA_TYPE = "varchar(50)";

    protected void setupDatabase(Database database) throws Exception {

        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null, null)
                .addColumn(COL_NAME, DATA_TYPE)
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null, null)
                .addColumn(COL_NAME, DATA_TYPE)
                , database);
    }

    protected SqlStatement createGeneratorUnderTest() {
        return new RenameColumnStatement(null, null, null, null, null);
    }

//    @Test
//    public void isValidGenerator() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//
//                if (database instanceof DB2Database
//                        || database instanceof CacheDatabase) {
//                    assertFalse(createGeneratorUnderTest().supportsDatabase(database));
//                } else {
//                    assertTrue(createGeneratorUnderTest().supportsDatabase(database));
//                }
//            }
//        });
//    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new RenameColumnStatement(null, TABLE_NAME, COL_NAME, NEW_COL_NAME, DATA_TYPE)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME));
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COL_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME));
                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COL_NAME));
                    }

                });
    }

     @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new RenameColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COL_NAME, NEW_COL_NAME, DATA_TYPE)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME));
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COL_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COL_NAME));
                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COL_NAME));
                    }

                });
    }

}
