package liquibase.dbtest.sqlite;

import liquibase.dbtest.AbstractIntegrationTest;

public class SQLiteIntegrationTest extends AbstractIntegrationTest {

	public SQLiteIntegrationTest() throws Exception {
        super("sqlite", "jdbc:sqlite:sqlite/liquibase.db");
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
       // TODO:CORE-2033 we should be able to get this to work on Travis
        return false;
    }
}
