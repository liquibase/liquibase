package liquibase.statement.core;

import liquibase.statement.core.DeleteStatement;

public class DeleteStatementTest extends AbstractSqStatementTest<DeleteStatement>{
    @Override
    protected DeleteStatement createStatementUnderTest() {
        return new DeleteStatement(null, null);
    }
}
