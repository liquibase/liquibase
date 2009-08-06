package liquibase.statement.core;

import liquibase.statement.core.AddColumnStatement;

public class AddColumnStatementTest extends AbstractSqStatementTest<AddColumnStatement> {

    @Override
    protected AddColumnStatement createStatementUnderTest() {
        return new AddColumnStatement(null, null, null, null, null);
    }
}