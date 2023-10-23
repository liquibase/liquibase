package liquibase.dbtest.db2z;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import org.junit.Ignore;

/**
 * Integration test for IBM's DB2Z database.
 */

@Ignore("No test database implementation")
public class DB2zIntegrationTest extends AbstractIntegrationTest {

    public DB2zIntegrationTest() throws Exception {
        super("db2z", DatabaseFactory.getInstance().getDatabase("db2z"));
    }
}
