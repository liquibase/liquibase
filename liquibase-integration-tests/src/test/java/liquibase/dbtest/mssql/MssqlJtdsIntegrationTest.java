package liquibase.dbtest.mssql;

import liquibase.database.DatabaseFactory;

public class MssqlJtdsIntegrationTest extends AbstractMssqlIntegrationTest {

    public MssqlJtdsIntegrationTest() throws Exception {
        /* @todo Need to extend the naming mechanism; this test needs a separate MSSQL Database to the JTDS test. */
        super("Mssql", DatabaseFactory.getInstance().getDatabase("mssql"));
    }
}
