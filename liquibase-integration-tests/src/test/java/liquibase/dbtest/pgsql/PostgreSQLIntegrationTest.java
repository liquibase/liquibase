package liquibase.dbtest.pgsql;

import liquibase.dbtest.AbstractIntegrationTest;
import org.junit.Test;

public class PostgreSQLIntegrationTest extends AbstractIntegrationTest {

    public PostgreSQLIntegrationTest() throws Exception {
        super("pgsql", "jdbc:postgresql://"+ getDatabaseServerHostname("PostgreSQL") +"/liquibase");
    }

    @Override
    @Test
    public void testRunChangeLog() throws Exception {
        super.testRunChangeLog();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
