package liquibase.dbtest.derby;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class DerbySampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public DerbySampleChangeLogRunnerTest() throws Exception {
        super("derby", "jdbc:derby:liquibase;create=true");
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }
    
}
