package liquibase.statement.core;

import liquibase.statement.core.RenameColumnStatement;

public class RenameColumnStatementTest extends AbstractSqStatementTest<RenameColumnStatement> {

    @Override
    protected RenameColumnStatement createStatementUnderTest() {
        return new RenameColumnStatement(null, null, null, null, null);
    }


}
