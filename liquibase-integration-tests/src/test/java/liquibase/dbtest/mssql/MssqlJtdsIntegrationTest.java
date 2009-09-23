package liquibase.dbtest.mssql;

import liquibase.dbtest.AbstractIntegrationTest;

public class MssqlJtdsIntegrationTest extends AbstractIntegrationTest {

    public MssqlJtdsIntegrationTest() throws Exception {
        super("mssql", "jdbc:jtds:sqlserver://"+ getDatabaseServerHostname() +";databaseName=liquibase");
    }
}
