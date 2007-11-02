package liquibase.database.sql;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import liquibase.database.*;
import liquibase.database.template.JdbcTemplate;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.DatabaseTest;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class CreateIndexStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "CreateIndexTest";
    private static final String INDEX_NAME = "IDX_CreateIndexTest";
    private static final String COLUMN_NAME = "testCol";
    private static final String COLUMN_NAME2 = "testCol2";

    @Before
    public void dropTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int")
                    .addColumn(COLUMN_NAME, "varchar(50)")
                    .addColumn(COLUMN_NAME2, "varchar(50)")
                    , database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn(COLUMN_NAME, "varchar(50)")
                        .addColumn(COLUMN_NAME2, "varchar(50)")
                        , database);
            }
        }
    }

    protected CreateIndexStatement generateTestStatement() {
        return new CreateIndexStatement(null, null, null);
    }

    @Test
    public void execute_singleColumnDefaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                CreateIndexStatement statement = new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, COLUMN_NAME);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getIndex(INDEX_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getIndex(INDEX_NAME));
                assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
            }
        });
    }

    @Test
    public void execute_alternateSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                CreateIndexStatement statement = new CreateIndexStatement(INDEX_NAME, TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNull(snapshot.getIndex(INDEX_NAME));

                new JdbcTemplate(database).execute(statement);

                //todo: assert that index was created in the correct location.  What schema does it go in?
//                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
//                assertNotNull(snapshot.getIndex(INDEX_NAME));
//                assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
            }
        });
    }

    @Test
    public void execute_alternateTablespace() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                CreateIndexStatement statement = new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, COLUMN_NAME).setTablespace(TestContext.ALT_TABLESPACE);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getIndex(INDEX_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getIndex(INDEX_NAME));
                assertEquals(COLUMN_NAME.toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
                //todo: assert tablespace location
            }
        });
    }

    @Test
    public void execute_multiColumnDefaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                CreateIndexStatement statement = new CreateIndexStatement(INDEX_NAME, null, TABLE_NAME, COLUMN_NAME, COLUMN_NAME2);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getIndex(INDEX_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                assertNotNull(snapshot.getIndex(INDEX_NAME));
                assertEquals((COLUMN_NAME+", "+COLUMN_NAME2).toUpperCase(), snapshot.getIndex(INDEX_NAME).getColumnNames().toUpperCase());
            }
        });
    }
}
