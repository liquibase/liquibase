package liquibase.statement.core;

import liquibase.statement.core.SetTableRemarksStatement;

public class SetTableRemarksStatementTest extends AbstractSqStatementTest<SetTableRemarksStatement> {
    @Override
    protected SetTableRemarksStatement createStatementUnderTest() {
        return new SetTableRemarksStatement(null, null, null);
    }
}
