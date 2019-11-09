package liquibase.dbtest.mssql;

import liquibase.database.DatabaseFactory;
import org.junit.Ignore;

@Ignore
public class MssqlCaseSensitiveIntegrationTest extends AbstractMssqlIntegrationTest {

    public MssqlCaseSensitiveIntegrationTest() throws Exception {
        /* @todo Need to extend the naming mechanism; this test needs a separate MSSQL Database to the CS test. */
        super("mssql", DatabaseFactory.getInstance().getDatabase("mssql"));
    } 
}
