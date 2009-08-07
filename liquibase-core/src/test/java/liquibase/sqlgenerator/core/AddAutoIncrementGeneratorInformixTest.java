package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;
import static org.junit.Assert.*;

public class AddAutoIncrementGeneratorInformixTest extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorInformixTest() throws Exception {
        super(new AddAutoIncrementGeneratorInformix());
    }

    @Override
    public void isValid() throws Exception {
        assertTrue(generatorUnderTest.validate(new AddAutoIncrementStatement(null, null, null, null), new InformixDatabase(), new MockSqlGeneratorChain()).getErrorMessages().contains("columnDataType is required"));
        assertFalse(generatorUnderTest.validate(new AddAutoIncrementStatement(null, "table_name", "column_name", "int"), new InformixDatabase(), new MockSqlGeneratorChain()).hasErrors());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof InformixDatabase;
    }
}