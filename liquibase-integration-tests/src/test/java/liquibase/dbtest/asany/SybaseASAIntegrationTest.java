package liquibase.dbtest.asany;

import liquibase.dbtest.AbstractIntegrationTest;

public class SybaseASAIntegrationTest extends AbstractIntegrationTest {

    public SybaseASAIntegrationTest() throws Exception {
        super( "asany", "jdbc:sybase:Tds:"+ getDatabaseServerHostname("SybaseASA") +":9810/servicename=prior");
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
