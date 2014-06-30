package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class CreateSequenceStatementTest extends AbstractStatementTest<CreateSequenceStatement> {

    @Override
    protected CreateSequenceStatement createObject() {
        return new CreateSequenceStatement(null, null, null);
    }


}