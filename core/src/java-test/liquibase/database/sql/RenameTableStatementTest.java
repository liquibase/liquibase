package liquibase.database.sql;

import liquibase.database.CacheDatabase;
import liquibase.database.Database;
import liquibase.database.FirebirdDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class RenameTableStatementTest  extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "RenameColumnTest";
    private static final String NEW_TABLE_NAME = "RenameColumnTest_new";

    protected void setupDatabase(Database database) throws Exception {

        dropTableIfExists(null, NEW_TABLE_NAME, database);
        dropTableIfExists(TestContext.ALT_SCHEMA, NEW_TABLE_NAME, database);

        dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                , database);
    }

    protected SqlStatement generateTestStatement() {
        return new RenameTableStatement(null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new RenameTableStatement(null, TABLE_NAME, NEW_TABLE_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME));
                        assertNull(snapshot.getTable(NEW_TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                        assertNotNull(snapshot.getTable(NEW_TABLE_NAME));
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new RenameTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME, NEW_TABLE_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME));
                        assertNull(snapshot.getTable(NEW_TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME));
                        assertNotNull(snapshot.getTable(NEW_TABLE_NAME));
                    }

                });
    }

     @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {

                if (database instanceof CacheDatabase || database instanceof FirebirdDatabase) {
                    assertFalse(generateTestStatement().supportsDatabase(database));
                } else {
                    assertTrue(generateTestStatement().supportsDatabase(database));
                }
            }
        });
    }

}
