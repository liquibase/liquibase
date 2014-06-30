package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropSequenceStatementTest extends AbstractStatementTest<DropSequenceStatement> {

    @Override
    protected DropSequenceStatement createObject() {
        return new DropSequenceStatement(null, null, null);
    }

}
