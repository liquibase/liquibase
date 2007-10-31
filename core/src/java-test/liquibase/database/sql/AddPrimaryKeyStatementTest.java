package liquibase.database.sql;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.DatabaseTest;
import liquibase.exception.JDBCException;

public class AddPrimaryKeyStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "AddPKTest";
    private static final String COLUMN_NAME = "id";
    private static final String COLUMN2_NAME = "id2";

    @Before
    @After
    public void dropAndCreateTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                    .addColumn(COLUMN_NAME, "int", new NotNullConstraint())
                    .addColumn(COLUMN2_NAME, "int", new NotNullConstraint()), database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addColumn(COLUMN_NAME, "int", new NotNullConstraint())
                        .addColumn(COLUMN2_NAME, "int", new NotNullConstraint()), database);
            }
        }
    }

    @Test
    public void getEndDelimiter() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                assertEquals(";", new AddPrimaryKeyStatement(null, null, null, null).getEndDelimiter(database));
            }
        });
    }

    @Test
    public void execute_noSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());

                new JdbcTemplate(database).execute(new AddPrimaryKeyStatement(null, TABLE_NAME, COLUMN_NAME, "PK_addpktest"));

                snapshot = new DatabaseSnapshot(database);
                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
            }
        });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());

                new JdbcTemplate(database).execute(new AddPrimaryKeyStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, "PK_addpktest"));

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
            }
        });
    }

     @Test
    public void execute_compundPKNoSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());

                new JdbcTemplate(database).execute(new AddPrimaryKeyStatement(null, TABLE_NAME, COLUMN_NAME+","+COLUMN2_NAME, "PK_addpktest"));

                snapshot = new DatabaseSnapshot(database);
                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
            }
        });
    }

    @Test
    public void execute_compundPKAltSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());

                new JdbcTemplate(database).execute(new AddPrimaryKeyStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME+","+COLUMN2_NAME, "PK_addpktest"));

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isPrimaryKey());
                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN2_NAME).isPrimaryKey());
            }
        });
    }
}
