package liquibase.dbtest.sqlite;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class SQLiteSampleChangeLogRunnerTest extends 
		AbstractSimpleChangeLogRunnerTest {

	public SQLiteSampleChangeLogRunnerTest() throws Exception {
        super("sqlite", "jdbc:sqlite:/liquibase.db");
    }
	
}
