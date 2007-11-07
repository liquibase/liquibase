package liquibase.migrator.derby;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class DerbySampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public DerbySampleChangeLogRunnerTest() throws Exception {
        super("derby", "jdbc:derby:liquibase;create=true");
    }

    protected boolean shouldRollBack() {
        return false;
    }
    
}
