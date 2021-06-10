package liquibase.dbtest.db2z;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;

/**
 * Integration test for IBM's DB2Z database.
 */

public class DB2zIntegrationTest extends AbstractIntegrationTest {

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        return false;
    }

    public DB2zIntegrationTest() throws Exception {
        super("db2z", DatabaseFactory.getInstance().getDatabase("db2z"));
    }
}
