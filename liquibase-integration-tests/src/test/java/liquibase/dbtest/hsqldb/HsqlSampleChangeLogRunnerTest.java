package liquibase.dbtest.hsqldb;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class HsqlSampleChangeLogRunnerTest  extends AbstractSimpleChangeLogRunnerTest {

    public HsqlSampleChangeLogRunnerTest() throws Exception {
        super("hsqldb", "jdbc:hsqldb:mem:liquibase");
    }

}
