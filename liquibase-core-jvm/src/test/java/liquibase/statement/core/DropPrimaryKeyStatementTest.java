package liquibase.statement.core;

import liquibase.statement.core.DropPrimaryKeyStatement;

public class DropPrimaryKeyStatementTest extends AbstractSqStatementTest<DropPrimaryKeyStatement> {

    @Override
    protected DropPrimaryKeyStatement createStatementUnderTest() {
        return new DropPrimaryKeyStatement(null, null, null);
    }

}
