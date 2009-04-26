package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.AddForeignKeyConstraintStatement;
import liquibase.database.statement.AbstractSqStatementTest;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.ForeignKey;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

import java.sql.DatabaseMetaData;

public class AddForeignKeyConstraintGeneratorTest extends AbstractSqStatementTest {

    private static final String FK_NAME = "FK_ADDTEST";

    private static final String BASE_TABLE_NAME = "AddFKTest";
    private static final String REF_TABLE_NAME = "AddFKTestRef";
    private static final String BASE_COLUMN_NAME = "NewCol";
    private static final String REF_COL_NAME = "id";

    protected void setupDatabase(Database database) throws Exception {
            dropAndCreateTable(new CreateTableStatement(null, BASE_TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int", null, null)
                    .addColumn(BASE_COLUMN_NAME, "int"), database);

            dropAndCreateTable(new CreateTableStatement(null, REF_TABLE_NAME)
                    .addPrimaryKeyColumn(REF_COL_NAME, "int", null, null)
                    .addColumn("existingCol", "int"), database);

            dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, BASE_TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int",null,  null)
                    .addColumn(BASE_COLUMN_NAME, "int"), database);

            dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, REF_TABLE_NAME)
                    .addPrimaryKeyColumn(REF_COL_NAME, "int", null, null)
                    .addColumn("existingCol", "int"), database);
    }

    protected AddForeignKeyConstraintStatement createGeneratorUnderTest() {
        return new AddForeignKeyConstraintStatement(null, null, null, null, null, null, null);
    }

    @Test
    public void execute() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddForeignKeyConstraintStatement(FK_NAME,
                        null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
                        null, REF_TABLE_NAME, REF_COL_NAME)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getForeignKey(FK_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
                        assertNotNull(fkSnapshot);
                        assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
                        assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumns().toUpperCase());
                        assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
                        assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumns().toUpperCase());
                        assertFalse(fkSnapshot.isDeferrable());
                        assertFalse(fkSnapshot.isInitiallyDeferred());
                    }

                });
    }

    @Test
    public void execute_deferrable() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddForeignKeyConstraintStatement(FK_NAME,
                        null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
                        null, REF_TABLE_NAME, REF_COL_NAME)
                        .setDeferrable(true)
                        .setInitiallyDeferred(true)) {
                    protected boolean expectedException(Database database, JDBCException exception) {
                        return !database.supportsInitiallyDeferrableColumns();
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getForeignKey(FK_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
                        assertNotNull(fkSnapshot);
                        assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
                        assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumns().toUpperCase());
                        assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
                        assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumns().toUpperCase());
                        assertTrue(fkSnapshot.isDeferrable());
                        assertTrue(fkSnapshot.isInitiallyDeferred());
                    }
                });
    }

    @Test
    public void execute_deleteCascade() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(null, new AddForeignKeyConstraintStatement(FK_NAME,
                null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
                null, REF_TABLE_NAME, REF_COL_NAME).setDeleteRule(DatabaseMetaData.importedKeyCascade)) {
            protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                assertNull(snapshot.getForeignKey(FK_NAME));
            }

            protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
                assertNotNull(fkSnapshot);
                assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
                assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumns().toUpperCase());
                assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
                assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumns().toUpperCase());
                assertFalse(fkSnapshot.isDeferrable());
                assertFalse(fkSnapshot.isInitiallyDeferred());
            }

        });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddForeignKeyConstraintStatement(FK_NAME,
                        TestContext.ALT_SCHEMA, BASE_TABLE_NAME, BASE_COLUMN_NAME,
                        TestContext.ALT_SCHEMA, REF_TABLE_NAME, REF_COL_NAME)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getForeignKey(FK_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
                        assertNotNull(fkSnapshot);
                        assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
                        assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumns().toUpperCase());
                        assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
                        assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumns().toUpperCase());
                        assertFalse(fkSnapshot.isDeferrable());
                        assertFalse(fkSnapshot.isInitiallyDeferred());
                    }

                });
    }
}
