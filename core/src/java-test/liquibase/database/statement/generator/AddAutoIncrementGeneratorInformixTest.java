package liquibase.database.statement.generator;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.*;

public class AddAutoIncrementGeneratorInformixTest extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorInformixTest() {
        super(new AddAutoIncrementGeneratorInformix());
    }

    @Test
    public void generateSql_noSchema() throws Exception {
        testSqlOnAll("ALTER TABLE [table_name] MODIFY [column_name] serial", new AddAutoIncrementStatement(null, "table_name", "column_name", "int"));
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