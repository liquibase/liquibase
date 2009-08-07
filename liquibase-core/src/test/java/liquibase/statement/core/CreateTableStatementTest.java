package liquibase.statement.core;

import liquibase.statement.core.CreateTableStatement;

public class CreateTableStatementTest extends AbstractSqStatementTest<CreateTableStatement> {

    @Override
    protected CreateTableStatement createStatementUnderTest() {
        return new CreateTableStatement(null, null);
    }

}
