package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.H2Database;

public class AddAutoIncrementGeneratorHsqlTest extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorHsqlTest() throws Exception {
    	super(new AddAutoIncrementGeneratorHsql());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof H2Database;
    }
}