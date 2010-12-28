package liquibase.dbtest.mssql;

public class MssqlCaseSensitiveIntegrationTest extends AbstractMssqlIntegrationTest {

    public MssqlCaseSensitiveIntegrationTest() throws Exception {
        super("Mssql", "jdbc:sqlserver://"+ getDatabaseServerHostname("Mssql") +":1433;databaseName=LiquibaseCS");
    } 
}
