package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddUniqueConstraintGeneratorTest extends AbstractSqStatementTest {
    private static final String TABLE_NAME = "AddUQTest";
    private static final String COLUMN_NAME = "colToMakeUQ";

    protected void setupDatabase(Database database) throws Exception {
            dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                    .addColumn("id", "int", new NotNullConstraint())
                    .addColumn(COLUMN_NAME, "int", new NotNullConstraint()), database);

            dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                    .addColumn("id", "int", new NotNullConstraint())
                    .addColumn(COLUMN_NAME, "int", new NotNullConstraint()), database);
    }

    protected SqlStatement createGeneratorUnderTest() {
        return new AddUniqueConstraintStatement(null, null, null, null);
    }

    @Test
    public void execute_noSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest")) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
                        // snapshot = new DatabaseSnapshot(database);
//                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
                    }
                });
    }

    @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddUniqueConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, "uq_adduqtest")) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
//                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
                    }

                });
    }

    @Test
    public void execute_withTablespace() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest").setTablespace(TestContext.ALT_TABLESPACE)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
                        // snapshot = new DatabaseSnapshot(database);
//                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
                    }
                });
    }

}
