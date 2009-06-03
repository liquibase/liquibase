package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.H2Database;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorHsql;

public class AddAutoIncrementGeneratorHsqlTest extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorHsqlTest() throws Exception {
    	super(new AddAutoIncrementGeneratorHsql());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof H2Database;
    }
}