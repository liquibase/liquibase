package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropPrimaryKeyStatementTest extends AbstractStatementTest<DropPrimaryKeyStatement> {

    @Override
    protected DropPrimaryKeyStatement createObject() {
        return new DropPrimaryKeyStatement(null, null, null, null);
    }

}
