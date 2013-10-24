package liquibase.dbtest.sqlite;

import liquibase.dbtest.AbstractIntegrationTest;

public class SQLiteIntegrationTest extends AbstractIntegrationTest {

	public SQLiteIntegrationTest() throws Exception {
        super("sqlite", "jdbc:sqlite:sqlite/liquibase.db");
    }

}
