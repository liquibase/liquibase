package liquibase.dbtest.db2;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;

/**
 * Integration test für IBM's DB2 database.
 */

public class DB2IntegrationTest extends AbstractIntegrationTest {

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        return false;
    }

    public DB2IntegrationTest() throws Exception {
        super("db2", DatabaseFactory.getInstance().getDatabase("db2"));
    }
}
