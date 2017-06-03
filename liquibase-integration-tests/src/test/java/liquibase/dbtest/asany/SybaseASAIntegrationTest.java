package liquibase.dbtest.asany;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;

public class SybaseASAIntegrationTest extends AbstractIntegrationTest {

    public SybaseASAIntegrationTest() throws Exception {
        super( "asany", DatabaseFactory.getInstance().getDatabase("asany"));
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
