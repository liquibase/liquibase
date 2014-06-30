package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropViewStatementTest extends AbstractStatementTest<DropViewStatement> {

    @Override
    protected DropViewStatement createObject() {
        return new DropViewStatement(null, null, null);
    }


}
