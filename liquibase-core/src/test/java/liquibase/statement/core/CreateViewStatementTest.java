package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class CreateViewStatementTest extends AbstractStatementTest<CreateViewStatement> {

    @Override
    protected CreateViewStatement createObject() {
        return new CreateViewStatement(null, null, null, null, false);
    }

}