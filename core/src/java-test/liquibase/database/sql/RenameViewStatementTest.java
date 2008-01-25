package liquibase.database.sql;

import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class RenameViewStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "RenameViewTestTable";
    private static final String VIEW_NAME = "RenameViewTest";
    private static final String NEW_VIEW_NAME = "RenameViewTest_new";

    protected void setupDatabase(Database database) throws Exception {

        dropViewIfExists(null, NEW_VIEW_NAME, database);

        dropViewIfExists(TestContext.ALT_SCHEMA, NEW_VIEW_NAME, database);

        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                , database);

        dropAndCreateView(new CreateViewStatement(null, VIEW_NAME, "select * from " + TABLE_NAME, false), database);
        dropAndCreateView(new CreateViewStatement(TestContext.ALT_SCHEMA, VIEW_NAME, "select * from " + TABLE_NAME, false), database);
    }

    protected SqlStatement generateTestStatement() {
        return new RenameViewStatement(null, null, null);
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {

                if (database instanceof DerbyDatabase
                        || database instanceof HsqlDatabase
                        || database instanceof DB2Database
                        || database instanceof CacheDatabase
                        || database instanceof FirebirdDatabase) {
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
                new SqlStatementDatabaseTest(null, new RenameViewStatement(null, VIEW_NAME, NEW_VIEW_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getView(VIEW_NAME));
                        assertNull(snapshot.getView(NEW_VIEW_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getView(VIEW_NAME));
                        assertNotNull(snapshot.getView(NEW_VIEW_NAME));
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new RenameViewStatement(TestContext.ALT_SCHEMA, VIEW_NAME, NEW_VIEW_NAME)) {

                    protected boolean expectedException(Database database, JDBCException exception) {
                        return database instanceof OracleDatabase || !database.supportsSchemas();
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getView(VIEW_NAME));
                        assertNull(snapshot.getView(NEW_VIEW_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getView(VIEW_NAME));
                        assertNotNull(snapshot.getView(NEW_VIEW_NAME));
                    }

                });
    }

}
