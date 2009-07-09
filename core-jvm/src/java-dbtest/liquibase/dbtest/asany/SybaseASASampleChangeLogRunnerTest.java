package liquibase.dbtest.asany;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class SybaseASASampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public SybaseASASampleChangeLogRunnerTest() throws Exception {
        super( "asany", "jdbc:sybase:Tds:localhost:9810/servicename=prior");
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }
    
}
