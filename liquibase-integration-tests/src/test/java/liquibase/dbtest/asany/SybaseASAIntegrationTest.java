package liquibase.dbtest.asany;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import org.junit.Ignore;

@Ignore("No test database implementation")
public class SybaseASAIntegrationTest extends AbstractIntegrationTest {

    public SybaseASAIntegrationTest() throws Exception {
        super( "asany", DatabaseFactory.getInstance().getDatabase("asany"));
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }
    
}
