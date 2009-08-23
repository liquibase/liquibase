package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.H2Database;

public class AddAutoIncrementGeneratorHsqlH2Test extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorHsqlH2Test() throws Exception {
    	super(new AddAutoIncrementGeneratorHsqlH2l());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof HsqlDatabase || database instanceof H2Database;
    }
}