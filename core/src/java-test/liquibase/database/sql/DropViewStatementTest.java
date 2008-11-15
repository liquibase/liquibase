package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropViewStatementTest extends AbstractSqlStatementTest {
    private static final String VIEW_NAME = "dropviewtest";
    private static final String TABLE_NAME = "dropviewtesttable";

    protected void setupDatabase(Database database) throws Exception {

        dropViewIfExists(null, VIEW_NAME, database);

        dropViewIfExists(TestContext.ALT_SCHEMA, VIEW_NAME, database);

        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null, null)
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addPrimaryKeyColumn("id", "int", null, null)
                , database);

        new JdbcTemplate(database).execute(new CreateViewStatement(null, VIEW_NAME, "select * from "+TABLE_NAME, false));

        if (database.supportsSchemas()) {
            new JdbcTemplate(database).execute(new CreateViewStatement(TestContext.ALT_SCHEMA, VIEW_NAME, "select * from "+ TestContext.ALT_SCHEMA+"."+TABLE_NAME, false));
        }
    }

    protected SqlStatement generateTestStatement() {
        return new DropViewStatement(null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new DropViewStatement(null, VIEW_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getView(VIEW_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getView(VIEW_NAME));
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropViewStatement(TestContext.ALT_SCHEMA, VIEW_NAME)) {

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNotNull(snapshot.getView(VIEW_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getView(VIEW_NAME));
                    }

                });
    }

}
