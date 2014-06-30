package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropIndexStatementTest extends AbstractStatementTest<DropIndexStatement> {

    @Override
    protected DropIndexStatement createObject() {
        return new DropIndexStatement(null, null, null, null, null);
    }

}
