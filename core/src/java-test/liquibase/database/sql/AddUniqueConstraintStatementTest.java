package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AddUniqueConstraintStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "AddUQTest";
    private static final String COLUMN_NAME = "colToMakeUQ";

    @Before
    @After
    public void dropAndCreateTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                    .addColumn("id", "int", new NotNullConstraint())
                    .addColumn(COLUMN_NAME, "int", new NotNullConstraint()), database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addColumn("id", "int", new NotNullConstraint())
                        .addColumn(COLUMN_NAME, "int", new NotNullConstraint()), database);
            }
        }
    }

    protected SqlStatement generateTestStatement() {
        return new AddUniqueConstraintStatement(null, null, null, null);
    }

    @Test
    public void execute_noSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());

                new JdbcTemplate(database).execute(new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest"));

                //todo: enable snapshot and assertion when snapshot can check for unique constraints
                // snapshot = new DatabaseSnapshot(database);
//                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
            }
        });
    }

     @Test
    public void execute_withSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());

                new JdbcTemplate(database).execute(new AddUniqueConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, "uq_adduqtest"));

                //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
//                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
            }
        });
    }

    @Test
    public void execute_withTablespace() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                if (!database.supportsTablespaces()) {
                    return;
                }

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());

                AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest");
                statement.setTablespace(TestContext.ALT_TABLESPACE);
                new JdbcTemplate(database).execute(statement);

                //todo: enable snapshot and assertion when snapshot can check for unique constraints
                // snapshot = new DatabaseSnapshot(database);
//                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
            }
        });
    }

}
