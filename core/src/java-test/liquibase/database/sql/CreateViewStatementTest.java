package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.HsqlDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.View;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CreateViewStatementTest extends AbstractSqlStatementTest {

    private static final String VIEW_NAME = "CreateViewTest";
    private static final String TABLE_NAME = "CreateViewTestTable";

    protected SqlStatement generateTestStatement() {
        return new CreateViewStatement(null, null, null);
    }

    @Before
    public void dropView() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropViewIfExists(null, VIEW_NAME, database);

            dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                    .addPrimaryKeyColumn("id", "int")
                    .addColumn("name", "varchar(50)")
                    , database);

            if (database.supportsSchemas()) {
                dropViewIfExists(TestContext.ALT_SCHEMA, VIEW_NAME, database);

                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addPrimaryKeyColumn("id", "int")
                        .addColumn("name", "varchar(50)")
                        , database);
            }
        }
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                String definition = "SELECT * FROM " + TABLE_NAME;
                CreateViewStatement statement = new CreateViewStatement(null, VIEW_NAME, definition);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getView(VIEW_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database);
                View view = snapshot.getView(VIEW_NAME);
                assertNotNull(view);
                assertEquals(2, view.getColumns().size());
            }
        });
    }
    
     @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                if (!database.supportsSchemas() || database instanceof HsqlDatabase) {
                    return;
                }
                
                String definition = "SELECT * FROM " + TABLE_NAME;
                CreateViewStatement statement = new CreateViewStatement(TestContext.ALT_SCHEMA, VIEW_NAME, definition);

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
                assertNull(snapshot.getView(VIEW_NAME));

                new JdbcTemplate(database).execute(statement);

                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);

                View view = snapshot.getView(VIEW_NAME);
                assertNotNull(view);
                assertEquals(2, view.getColumns().size());
            }
        });
    }
}
