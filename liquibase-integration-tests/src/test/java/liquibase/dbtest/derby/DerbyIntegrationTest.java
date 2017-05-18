package liquibase.dbtest.derby;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;

public class DerbyIntegrationTest extends AbstractIntegrationTest {

    
    public DerbyIntegrationTest() throws Exception {
        super("derby", DatabaseFactory.getInstance().getDatabase("derby"));
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }
    
}
