package liquibase.dbtest.mssql;

public class MssqlIntegrationTest extends AbstractMssqlIntegrationTest {

    public MssqlIntegrationTest() throws Exception {
        super("Mssql", "jdbc:sqlserver://"+ getDatabaseServerHostname("Mssql") +":1433;databaseName=liquibase");
    }

    @Override
    protected boolean supportsAltCatalogTests() {
        return false;
    }
}
