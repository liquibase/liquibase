package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.DatabaseTest;
import liquibase.change.CreateTableChange;
import liquibase.change.ColumnConfig;

import java.sql.Types;
import java.util.List;

import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

public class UpdateStatementTest {

    @BeforeClass
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
