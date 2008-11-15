package liquibase.dbtest.asany;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class SybaseASASampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public SybaseASASampleChangeLogRunnerTest() throws Exception {
        super( "asany", "jdbc:sybase:Tds:localhost:9810/servicename=prior");
    }


    protected void setUp() throws Exception {
        super.setUp();
    }


    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected boolean shouldRollBack() {
        return false;
    }
    
}
