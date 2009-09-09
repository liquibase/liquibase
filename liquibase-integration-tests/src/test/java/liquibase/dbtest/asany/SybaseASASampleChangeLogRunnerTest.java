package liquibase.dbtest.asany;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class SybaseASASampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public SybaseASASampleChangeLogRunnerTest() throws Exception {
        super( "asany", "jdbc:sybase:Tds:localhost:9810/servicename=prior");
    }


    @Override
    protected boolean shouldRollBack() {
        return false;
    }
    
}
