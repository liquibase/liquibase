package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.ForeignKey;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AddForeignKeyConstraintChangeStatementTest extends AbstractSqlStatementTest {

    private static final String FK_NAME = "FK_ADDTEST";

    private static final String BASE_TABLE_NAME = "AddFKTest";
    private static final String REF_TABLE_NAME = "AddFKTestRef";
    private static final String BASE_COLUMN_NAME = "NewCol";
    private static final String REF_COL_NAME = "id";

    @Before
    public void dropAndCreateTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            dropAndCreateTable(new CreateTableStatement(BASE_TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int")
                    .addColumn(BASE_COLUMN_NAME, "int"), database);

            dropAndCreateTable(new CreateTableStatement(REF_TABLE_NAME)
                    .addPrimaryKeyColumn(REF_COL_NAME, "int")
                    .addColumn("existingCol", "int"), database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, BASE_TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn(BASE_COLUMN_NAME, "int"), database);

                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, REF_TABLE_NAME)
                        .addPrimaryKeyColumn(REF_COL_NAME, "int")
                        .addColumn("existingCol", "int"), database);
            }
        }
    }

    protected AddForeignKeyConstraintChangeStatement generateTestStatement() {
        return new AddForeignKeyConstraintChangeStatement(null, null, null, null, null, null, null);
    }

    @Test
    public void execute() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getForeignKey(FK_NAME));

                new JdbcTemplate(database).execute(new AddForeignKeyConstraintChangeStatement(FK_NAME,
                        null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
                        null, REF_TABLE_NAME, REF_COL_NAME));

                snapshot = new DatabaseSnapshot(database);
                ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
                assertNotNull(fkSnapshot);
                assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
                assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumn().toUpperCase());
                assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
                assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumn().toUpperCase());
                assertFalse(fkSnapshot.isDeferrable());
                assertFalse(fkSnapshot.isInitiallyDeferred());
            }
        });
    }

    @Test
    public void execute_deferrable() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                if (!database.supportsInitiallyDeferrableColumns()) {
                    return;
                }
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getForeignKey(FK_NAME));

                AddForeignKeyConstraintChangeStatement constraint = new AddForeignKeyConstraintChangeStatement(FK_NAME,
                        null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
                        null, REF_TABLE_NAME, REF_COL_NAME);
                constraint.setDeferrable(true);
                constraint.setInitiallyDeferred(true);

                new JdbcTemplate(database).execute(constraint);

                snapshot = new DatabaseSnapshot(database);
                ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
                assertNotNull(fkSnapshot);
                assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
                assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumn().toUpperCase());
                assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
                assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumn().toUpperCase());
                assertTrue(fkSnapshot.isDeferrable());
                assertTrue(fkSnapshot.isInitiallyDeferred());
            }
        });
    }

    @Test
    public void execute_deleteCascade() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getForeignKey(FK_NAME));

                AddForeignKeyConstraintChangeStatement statement = new AddForeignKeyConstraintChangeStatement(FK_NAME,
                        null, BASE_TABLE_NAME, BASE_COLUMN_NAME,
                        null, REF_TABLE_NAME, REF_COL_NAME);
                statement.setDeleteCascade(true);
                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
                assertNotNull(fkSnapshot);
                assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
                assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumn().toUpperCase());
                assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
                assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumn().toUpperCase());
                assertFalse(fkSnapshot.isDeferrable());
                assertFalse(fkSnapshot.isInitiallyDeferred());
            }
        });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNull(snapshot.getForeignKey(FK_NAME));

                new JdbcTemplate(database).execute(new AddForeignKeyConstraintChangeStatement(FK_NAME,
                        TestContext.ALT_SCHEMA, BASE_TABLE_NAME, BASE_COLUMN_NAME,
                        TestContext.ALT_SCHEMA, REF_TABLE_NAME, REF_COL_NAME));

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                ForeignKey fkSnapshot = snapshot.getForeignKey(FK_NAME);
                assertNotNull(fkSnapshot);
                assertEquals(BASE_TABLE_NAME.toUpperCase(), fkSnapshot.getForeignKeyTable().getName().toUpperCase());
                assertEquals(BASE_COLUMN_NAME.toUpperCase(), fkSnapshot.getForeignKeyColumn().toUpperCase());
                assertEquals(REF_TABLE_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyTable().getName().toUpperCase());
                assertEquals(REF_COL_NAME.toUpperCase(), fkSnapshot.getPrimaryKeyColumn().toUpperCase());
                assertFalse(fkSnapshot.isDeferrable());
                assertFalse(fkSnapshot.isInitiallyDeferred());
            }
        });
    }
}
