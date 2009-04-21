package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
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

    @Test
    public void constructorWithDelimiterPassed() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) {
                RawSqlStatement statement = new RawSqlStatement("statement here", "GO\n");
                assertEquals("GO\n", statement.getEndDelimiter(database));
            }
        });
    }

    @Test
    public void testToString() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) {
                RawSqlStatement statement = new RawSqlStatement("statement here");
                assertEquals("statement here", statement.toString());
            }
        });
    }
}
