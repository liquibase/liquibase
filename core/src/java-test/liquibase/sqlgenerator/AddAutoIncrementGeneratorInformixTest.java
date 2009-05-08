package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.statement.AddAutoIncrementStatement;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AddAutoIncrementGeneratorInformixTest extends AddAutoIncrementGeneratorTest<AddAutoIncrementStatement> {

    public AddAutoIncrementGeneratorInformixTest() throws Exception {
        super(new AddAutoIncrementGeneratorInformix());
    }

    @Override
    public void isValid() throws Exception {
        assertTrue(generatorUnderTest.validate(new AddAutoIncrementStatement(null, null, null, null), new InformixDatabase()).getErrorMessages().contains("columnDataType is required"));
        assertFalse(generatorUnderTest.validate(new AddAutoIncrementStatement(null, null, null, "int"), new InformixDatabase()).hasErrors());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof InformixDatabase;
    }
}