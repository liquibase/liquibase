package liquibase.database.sql;

import liquibase.change.CreateTableChange;
import liquibase.database.Database;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;

public class UpdateStatementTest {

    @Before
    public void setupTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            CreateTableChange createTableChange = new CreateTableChange();
            createTableChange.setTableName("updateStatementTest");
        }
    }

    @Test
    public void addNewColumnValue_nullValue() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {

            public void performTest(Database database) {
                UpdateStatement statement = new UpdateStatement();
                statement.setTableName("tableName");
                statement.addNewColumnValue("colName", null, Types.VARCHAR);

                assertEquals("UPDATE tableName SET colName = NULL", statement.getSqlStatement(database));
            }
        });
    }
}
