package liquibase.statement.core;

import liquibase.statement.core.RenameViewStatement;

public class RenameViewStatementTest extends AbstractSqStatementTest<RenameViewStatement> {

    @Override
    protected RenameViewStatement createStatementUnderTest() {
        return new RenameViewStatement(null, null, null);
    }


}
