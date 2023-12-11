package liquibase.dbtest.db2;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Integration test für IBM's DB2 database.
 */

public class DB2IntegrationTest extends AbstractIntegrationTest {

    public DB2IntegrationTest() throws Exception {
        super("db2", DatabaseFactory.getInstance().getDatabase("db2"));
    }
}
