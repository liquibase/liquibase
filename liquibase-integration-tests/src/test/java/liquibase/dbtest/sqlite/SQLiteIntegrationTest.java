package liquibase.dbtest.sqlite;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;

public class SQLiteIntegrationTest extends AbstractIntegrationTest {

	public SQLiteIntegrationTest() throws Exception {
        super("sqlite", DatabaseFactory.getInstance().getDatabase("sqlite"));
    }

}
