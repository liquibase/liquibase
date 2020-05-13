package liquibase.dbtest.pgsql;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;

public class PostgreSQLIntegrationTest extends AbstractIntegrationTest {

    public PostgreSQLIntegrationTest() throws Exception {
        super("pgsql", DatabaseFactory.getInstance().getDatabase("postgresql"));
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        return true;
    }

}
