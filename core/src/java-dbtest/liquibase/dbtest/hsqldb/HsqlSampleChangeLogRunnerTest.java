package liquibase.dbtest.hsqldb;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class HsqlSampleChangeLogRunnerTest  extends AbstractSimpleChangeLogRunnerTest {

    public HsqlSampleChangeLogRunnerTest() throws Exception {
        super("hsqldb", "jdbc:hsqldb:mem:liquibase");
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
