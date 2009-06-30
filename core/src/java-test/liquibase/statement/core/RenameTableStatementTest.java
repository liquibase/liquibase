package liquibase.statement.core;

import liquibase.statement.core.RenameTableStatement;

public class RenameTableStatementTest extends AbstractSqStatementTest<RenameTableStatement> {

    @Override
    protected RenameTableStatement createStatementUnderTest() {
        return new RenameTableStatement(null, null, null);
    }


}
