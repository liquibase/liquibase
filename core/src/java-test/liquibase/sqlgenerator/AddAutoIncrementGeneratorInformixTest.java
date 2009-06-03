package liquibase.sqlgenerator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.statement.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorInformixTest extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorInformixTest() throws Exception {
        super(new AddAutoIncrementGeneratorInformix());
    }

    @Override
    public void isValid() throws Exception {
        assertTrue(generatorUnderTest.validate(new AddAutoIncrementStatement(null, null, null, null), new InformixDatabase()).getErrorMessages().contains("columnDataType is required"));
        assertFalse(generatorUnderTest.validate(new AddAutoIncrementStatement(null, "table_name", "column_name", "int"), new InformixDatabase()).hasErrors());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof InformixDatabase;
    }
}