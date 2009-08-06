package liquibase.statement.core;

import liquibase.statement.core.DropViewStatement;

public class DropViewStatementTest extends AbstractSqStatementTest<DropViewStatement> {

    @Override
    protected DropViewStatement createStatementUnderTest() {
        return new DropViewStatement(null, null);
    }


}
