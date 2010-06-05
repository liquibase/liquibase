package liquibase.dbtest.mssql;

public class MssqlJtdsIntegrationTest extends AbstractMssqlIntegrationTest {

    public MssqlJtdsIntegrationTest() throws Exception {
        super("Mssql", "jdbc:jtds:sqlserver://"+ getDatabaseServerHostname("mssqlJtds") +";databaseName=liquibase");
    }
}
