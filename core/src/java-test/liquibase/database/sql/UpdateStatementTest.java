package liquibase.database.sql;

import liquibase.change.CreateTableChange;
import liquibase.database.Database;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.sql.Types;

public class UpdateStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "UpdateTest";
    private static final String COLUMN_NAME = "testCol";

    @Before
    @After
    public void dropTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {

            dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
                    .addColumn("id", "int")
                    .addColumn(COLUMN_NAME, "varchar(50)"), database);

            if (database.supportsSchemas()) {
                dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, "varchar(50)"), database);
            }
        }
    }

    protected SqlStatement generateTestStatement() {
        return new UpdateStatement(null, null);
    }

    @Test
    public void addNewColumnValue_nullValue() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {

            public void performTest(Database database) {
                UpdateStatement statement = new UpdateStatement(null, TABLE_NAME);
                statement.addNewColumnValue(COLUMN_NAME, null, Types.VARCHAR);

                assertEquals("UPDATE "+TABLE_NAME+" SET "+COLUMN_NAME+" = NULL", statement.getSqlStatement(database));
            }
        });
    }
}
