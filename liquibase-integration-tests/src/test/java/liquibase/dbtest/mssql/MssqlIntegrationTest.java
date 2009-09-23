package liquibase.dbtest.mssql;

import liquibase.dbtest.AbstractIntegrationTest;

public class MssqlIntegrationTest extends AbstractIntegrationTest {

    public MssqlIntegrationTest() throws Exception {
        super("mssql", "jdbc:sqlserver://"+ getDatabaseServerHostname() +";instanceName=SQL2005;databaseName=liquibase");
    }
}
