package liquibase.dbtest.derby;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class DerbySampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public DerbySampleChangeLogRunnerTest() throws Exception {
        super("derby", "jdbc:derby:liquibase;create=true");
    }

    protected boolean shouldRollBack() {
        return false;
    }
    
}
