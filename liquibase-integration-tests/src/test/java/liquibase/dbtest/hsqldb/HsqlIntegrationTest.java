package liquibase.dbtest.hsqldb;

import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.MigrationFailedException;
import org.junit.Test;

public class HsqlIntegrationTest extends AbstractIntegrationTest {

    public HsqlIntegrationTest() throws Exception {
        super("hsqldb", "jdbc:hsqldb:mem:liquibase");
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        return true;
    }
}
