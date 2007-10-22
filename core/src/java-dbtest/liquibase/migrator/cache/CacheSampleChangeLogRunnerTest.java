package liquibase.migrator.cache;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

public class CacheSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public CacheSampleChangeLogRunnerTest() throws Exception {
        super("cache", "jdbc:Cache://127.0.0.1:1972/liquibase");
    }

}
