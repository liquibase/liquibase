package liquibase.statement.core;

import liquibase.statement.core.SetColumnRemarksStatement;

public class SetColumnRemarksStatementTest extends AbstractSqStatementTest<SetColumnRemarksStatement> {
    @Override
    protected SetColumnRemarksStatement createStatementUnderTest() {
        return new SetColumnRemarksStatement(null, null, null, null);
    }
}
