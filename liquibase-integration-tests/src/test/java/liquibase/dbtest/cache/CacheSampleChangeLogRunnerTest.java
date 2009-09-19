package liquibase.dbtest.cache;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class CacheSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public CacheSampleChangeLogRunnerTest() throws Exception {
        super("cache", "jdbc:Cache://"+DATABASE_SERVER_HOSTNAME+":1972/liquibase");
    }

}
