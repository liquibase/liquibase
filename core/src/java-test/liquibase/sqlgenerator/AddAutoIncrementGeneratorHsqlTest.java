package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.H2Database;
import liquibase.statement.AddAutoIncrementStatement;
import org.junit.Test;

public class AddAutoIncrementGeneratorHsqlTest extends AddAutoIncrementGeneratorTest<AddAutoIncrementStatement> {

    public AddAutoIncrementGeneratorHsqlTest() throws Exception {
    	super(new AddAutoIncrementGeneratorHsql());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof H2Database;
    }
}