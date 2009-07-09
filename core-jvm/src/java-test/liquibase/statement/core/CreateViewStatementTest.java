package liquibase.statement.core;

import liquibase.statement.core.CreateViewStatement;

public class CreateViewStatementTest extends AbstractSqStatementTest<CreateViewStatement> {

    @Override
    protected CreateViewStatement createStatementUnderTest() {
        return new CreateViewStatement(null, null, null, false);
    }

}