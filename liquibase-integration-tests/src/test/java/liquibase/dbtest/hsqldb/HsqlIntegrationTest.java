package liquibase.dbtest.hsqldb;

import liquibase.dbtest.AbstractIntegrationTest;

public class HsqlIntegrationTest extends AbstractIntegrationTest {

    public HsqlIntegrationTest() throws Exception {
        super("hsqldb", "jdbc:hsqldb:mem:liquibase");
    }
}
