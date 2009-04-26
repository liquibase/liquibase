package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.DropColumnStatement;
import liquibase.database.statement.AbstractSqStatementTest;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropColumnGeneratorTest extends AbstractSqStatementTest {

    private static final String TABLE_NAME = "DropColumnTest";
    private static final String COLUMN_NAME = "testCol";

    protected void setupDatabase(Database database) throws Exception {
            dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int",null,  null)
                    .addColumn(COLUMN_NAME, "varchar(50)")
                    , database);

            dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int", null, null)
                    .addColumn(COLUMN_NAME, "varchar(50)")
                    , database);
    }

//    @Test
//    public void isValidGenerator() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                if (database instanceof SQLiteDatabase) {
//                    assertFalse(createGeneratorUnderTest().supportsDatabase(database));
//                } else {
//                    assertTrue(createGeneratorUnderTest().supportsDatabase(database));
//                }
//            }
//        });
//    }

    protected DropColumnStatement createGeneratorUnderTest() {
        return new DropColumnStatement(null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropColumnStatement(null, TABLE_NAME, COLUMN_NAME)) {

                    protected boolean supportsTest(Database database) {
                        return !(database instanceof MSSQLDatabase); //for some reason, the metadata isn't updated by mssql
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME));
                    }

                });

    }
}
