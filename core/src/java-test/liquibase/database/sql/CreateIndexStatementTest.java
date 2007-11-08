package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class CreateIndexStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "CreateIndexTest";
    private static final String INDEX_NAME = "IDX_CreateIndexTest";
    private static final String COLUMN_NAME = "testCol";
    private static final String COLUMN_NAME2 = "testCol2";

    protected void setupDatabase(Database database) throws Exception {
            dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int")
                    .addColumn(COLUMN_NAME, "varchar(50)")
                    .addColumn(COLUMN_NAME2, "varchar(50)")
                    , database);
            dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int")
                    .addColumn(COLUMN_NAME, "varchar(50)")
                    .addColumn(COLUMN_NAME2, "varchar(50)")
                    , database);
    }

    protected CreateIndexStatement generateTestStatement() {
        return new CreateIndexStatement(null, null, null);
    }

    @Test
    public void execute_singleColumnDefaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, COLUMN_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getIndex(INDEX_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getIndex(INDEX_NAME));
                        assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
                    }
                });
    }

    @Test
    public void execute_alternateSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateIndexStatement(INDEX_NAME, TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getIndex(INDEX_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        //todo: assert that index was created in the correct location.  What schema does it go in?
//                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
//                assertNotNull(snapshot.getIndex(INDEX_NAME));
//                assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
                    }
                });
    }

    @Test
    public void execute_alternateTablespace() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, COLUMN_NAME).setTablespace(TestContext.ALT_TABLESPACE)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getIndex(INDEX_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getIndex(INDEX_NAME));
                        assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
                        //todo: assert tablespace location
                    }
                });
    }

    @Test
    public void execute_multiColumnDefaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, COLUMN_NAME, COLUMN_NAME2)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getIndex(INDEX_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getIndex(INDEX_NAME));
                        assertEquals((COLUMN_NAME + ", " + COLUMN_NAME2).toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
                    }
                });
    }
}
