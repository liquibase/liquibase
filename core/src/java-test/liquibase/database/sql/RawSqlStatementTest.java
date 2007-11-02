package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.DatabaseTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RawSqlStatementTest extends AbstractSqlStatementTest {

    protected SqlStatement generateTestStatement() {
        return new RawSqlStatement(null);
    }

    protected void setupDatabase(Database database) throws Exception {
        ; //nothing to set up
    }

    @Test
    public void constructorWithNoDelimiterPassed() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) {                
                RawSqlStatement statement = new RawSqlStatement("statement here");
                assertEquals(";", statement.getEndDelimiter(database));
            }
        });
    }
    
    @Test
    public void constructor() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) {
            RawSqlStatement statement = new RawSqlStatement("statement here");
            assertEquals("statement here", statement.getSqlStatement(database));
            }
        });
    }
}
