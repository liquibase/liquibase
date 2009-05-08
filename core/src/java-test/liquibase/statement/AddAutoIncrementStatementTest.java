package liquibase.statement;

import org.junit.Test;

public class AddAutoIncrementStatementTest extends AbstractSqStatementTest<AddAutoIncrementStatement> {
    
    @Test
    public void nothing() {

    }

    protected AddAutoIncrementStatement createStatementUnderTest() {
        return new AddAutoIncrementStatement("SCHEMA_NAME", "TABLE_NAME", "COLUMN_NAME", "COLUMN_TYPE");
    }
}
