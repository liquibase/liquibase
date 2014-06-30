package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class SetTableRemarksStatementTest extends AbstractStatementTest<SetTableRemarksStatement> {
    @Override
    protected SetTableRemarksStatement createObject() {
        return new SetTableRemarksStatement(null, null, null, null);
    }
}
