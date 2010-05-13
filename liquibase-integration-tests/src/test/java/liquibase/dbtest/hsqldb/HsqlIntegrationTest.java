package liquibase.dbtest.hsqldb;

import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.MigrationFailedException;
import org.junit.Test;

public class HsqlIntegrationTest extends AbstractIntegrationTest {

    public HsqlIntegrationTest() throws Exception {
        super("hsqldb", "jdbc:hsqldb:mem:liquibase");
    }


    /**
     * We expect a DatabaseException because HSQLDB 1.8 doesn't support fks beetween schemas.<br/>
     */
    @Override
    @Test(expected=MigrationFailedException.class)
    public void testDiffExternalForeignKeys() throws Exception {
        super.testDiffExternalForeignKeys();
    }
}
