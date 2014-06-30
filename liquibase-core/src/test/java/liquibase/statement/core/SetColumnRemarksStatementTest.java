package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class SetColumnRemarksStatementTest extends AbstractStatementTest<SetColumnRemarksStatement> {
    @Override
    protected SetColumnRemarksStatement createObject() {
        return new SetColumnRemarksStatement(null, null, null, null, null);
    }
}
