package liquibase.dbtest.pgsql;

import liquibase.dbtest.AbstractIntegrationTest;
import org.junit.Test;

public class PostgreSQLIntegrationTest extends AbstractIntegrationTest {

    public PostgreSQLIntegrationTest() throws Exception {
        super("pgsql", "jdbc:postgresql://"+ getDatabaseServerHostname("PostgreSQL") +"/liquibase");
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        // TODO:CORE-2033 we should configure this in Travis and turn this test back on
        // See https://docs.travis-ci.com/user/database-setup/#PostgreSQL
        return false;
    }

}
