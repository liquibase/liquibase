package liquibase.dbtest.h2;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class H2SampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public H2SampleChangeLogRunnerTest() throws Exception {
        super("h2", "jdbc:h2:mem:liquibase");
    }

    @Override
    public void testUpdateTwice() throws Exception {
        super.testUpdateTwice();    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
