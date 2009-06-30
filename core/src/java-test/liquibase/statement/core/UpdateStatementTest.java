package liquibase.statement.core;

import liquibase.statement.core.UpdateStatement;

public class UpdateStatementTest extends AbstractSqStatementTest<UpdateStatement> {

    @Override
    protected UpdateStatement createStatementUnderTest() {
        return new UpdateStatement(null, null);
    }

}
