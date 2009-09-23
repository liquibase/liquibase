package liquibase.dbtest.cache;

import liquibase.dbtest.AbstractIntegrationTest;

public class CacheIntegrationTest extends AbstractIntegrationTest {

    public CacheIntegrationTest() throws Exception {
        super("cache", "jdbc:Cache://"+ getDatabaseServerHostname() +":1972/liquibase");
    }

}
