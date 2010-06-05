package liquibase.dbtest.mysql;

import liquibase.dbtest.AbstractIntegrationTest;

public class MySQLIntegrationTest extends AbstractIntegrationTest {

    public MySQLIntegrationTest() throws Exception {
        super("mysql", "jdbc:mysql://"+ getDatabaseServerHostname("MySQL") +"/liquibase");
    }
}
