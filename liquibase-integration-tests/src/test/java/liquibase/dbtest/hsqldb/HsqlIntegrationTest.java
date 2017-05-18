package liquibase.dbtest.hsqldb;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.MigrationFailedException;
import org.junit.Test;

public class HsqlIntegrationTest extends AbstractIntegrationTest {

    public HsqlIntegrationTest() throws Exception {
        super("hsqldb", DatabaseFactory.getInstance().getDatabase("hsqldb"));
    }
}
