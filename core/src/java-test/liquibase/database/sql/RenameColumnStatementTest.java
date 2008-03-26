package liquibase.database.sql;

import liquibase.database.CacheDatabase;
import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class RenameColumnStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "RenameColumnTest";
    private static final String COL_NAME = "testCol";
    private static final String NEW_COL_NAME = "newColName";
    private static final String DATA_TYPE = "varchar(50)";

    protected void setupDatabase(Database database) throws Exception {

        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null)
                .addColumn(COL_NAME, DATA_TYPE)
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null)
                .addColumn(COL_NAME, DATA_TYPE)
                , database);
    }

    protected SqlStatement generateTestStatement() {
        return new RenameColumnStatement(null, null, null, null, null);
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {

                if (database instanceof DB2Database
                        || database instanceof CacheDatabase
                        || database instanceof DerbyDatabase) {
                    assertFalse(generateTestStatement().supportsDatabase(database));
                } else {
                    assertTrue(generateTestStatement().supportsDatabase(database));
                }
            }
        });
    }

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
