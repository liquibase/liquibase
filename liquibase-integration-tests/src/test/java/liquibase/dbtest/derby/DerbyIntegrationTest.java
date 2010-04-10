package liquibase.dbtest.derby;

import liquibase.dbtest.AbstractIntegrationTest;

public class DerbyIntegrationTest extends AbstractIntegrationTest {

    
    public DerbyIntegrationTest() throws Exception {
        super("derby", "jdbc:derby:liquibase;create=true");
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }
    
}
