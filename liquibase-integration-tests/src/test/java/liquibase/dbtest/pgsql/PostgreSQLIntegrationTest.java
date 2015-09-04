package liquibase.dbtest.pgsql;

import liquibase.dbtest.AbstractIntegrationTest;

public class PostgreSQLIntegrationTest extends AbstractIntegrationTest {

    public PostgreSQLIntegrationTest() throws Exception {
        super("pgsql", "jdbc:postgresql://"+ getDatabaseServerHostname("PostgreSQL") +"/liquibase");
    }
}
