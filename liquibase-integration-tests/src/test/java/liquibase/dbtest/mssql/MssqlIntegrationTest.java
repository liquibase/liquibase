package liquibase.dbtest.mssql;

import liquibase.dbtest.AbstractIntegrationTest;

public class MssqlIntegrationTest extends AbstractIntegrationTest {

    public MssqlIntegrationTest() throws Exception {
        super("mssql", "jdbc:sqlserver://"+ getDatabaseServerHostname() +":1433;instanceName=SQLEXPRESS2005;databaseName=liquibase");
    }
}
