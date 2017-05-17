package liquibase.dbtest.derby;

import liquibase.dbtest.AbstractIntegrationTest;
import org.junit.Ignore;

@Ignore // FIXME: CORE-2033 this all fails for me with "Schema 'LIQUIBASE' does not exist"
public class DerbyIntegrationTest extends AbstractIntegrationTest {

    public DerbyIntegrationTest() throws Exception {
        super("derby", "jdbc:derby:liquibase;create=true");
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        // Derby is an in-process database
        return true;
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }
    
}
