package liquibase.statement.core;

import liquibase.statement.core.AddPrimaryKeyStatement;

public class AddPrimaryKeyStatementTest extends AbstractSqStatementTest<AddPrimaryKeyStatement> {

    @Override
    protected AddPrimaryKeyStatement createStatementUnderTest() {
        return new AddPrimaryKeyStatement(null, null, null, null);
    }

   
}
