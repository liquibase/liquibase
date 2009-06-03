package liquibase.sqlgenerator.core;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorDB2;

public class AddAutoIncrementGeneratorDB2Test extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorDB2Test() throws Exception {
    	super(new AddAutoIncrementGeneratorDB2());
    }

     @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof DB2Database;
    }
}