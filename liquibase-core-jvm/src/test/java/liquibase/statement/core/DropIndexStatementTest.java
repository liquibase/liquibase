package liquibase.statement.core;

import liquibase.statement.core.DropIndexStatement;

public class DropIndexStatementTest extends AbstractSqStatementTest<DropIndexStatement> {

    @Override
    protected DropIndexStatement createStatementUnderTest() {
        return new DropIndexStatement(null, null, null);
    }

}
