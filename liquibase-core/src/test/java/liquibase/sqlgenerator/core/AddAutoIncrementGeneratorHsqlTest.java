package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;

public class AddAutoIncrementGeneratorHsqlTest extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorHsqlTest() throws Exception {
    	super(new AddAutoIncrementGeneratorHsql());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof HsqlDatabase;
    }
}