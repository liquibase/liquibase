package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropDefaultValueStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "DropDefaultTest";
    private static final String COLUMN_NAME = "testCol";

    protected void setupDatabase(Database database) throws Exception {
            dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int", null, null)
                    .addColumn(COLUMN_NAME, "varchar(50)", "'Def Value'")
                    , database);

            dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int", null, null)
                    .addColumn(COLUMN_NAME, "varchar(50)", "'Def Value'")
                    , database);
    }

    protected DropDefaultValueStatement generateTestStatement() {
        return new DropDefaultValueStatement(null, null, null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, null)) {

                    protected boolean supportsTest(Database database) {
                        return !(database instanceof DerbyDatabase);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertEquals(null, snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropDefaultValueStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, null)) {

                    protected boolean supportsTest(Database database) {
                        return !(database instanceof DerbyDatabase);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }
                });
    }
}
