package liquibase.statement.core;

import org.junit.Test;

public class AddAutoIncrementStatementTest extends AbstractSqStatementTest<AddAutoIncrementStatement> {
    
    @Test
    public void nothing() {

    }

    @Override
    protected AddAutoIncrementStatement createStatementUnderTest() {
        return new AddAutoIncrementStatement("CATALOG_NAME", "SCHEMA_NAME", "TABLE_NAME", "COLUMN_NAME", "COLUMN_TYPE", null, null);
    }
}
