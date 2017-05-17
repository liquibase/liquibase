package liquibase.dbtest.derby;

import liquibase.dbtest.AbstractIntegrationTest;

public class DerbyIntegrationTest extends AbstractIntegrationTest {

    
    public DerbyIntegrationTest() throws Exception {
        super("derby", "jdbc:derby:liquibase;create=true");
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        // Seems unlikely to ever be provided by Travis, as it's not free
        return false;
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }
    
}
