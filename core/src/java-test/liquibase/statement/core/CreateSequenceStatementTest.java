package liquibase.statement.core;

import liquibase.statement.core.CreateSequenceStatement;

public class CreateSequenceStatementTest extends AbstractSqStatementTest<CreateSequenceStatement> {

    @Override
    protected CreateSequenceStatement createStatementUnderTest() {
        return new CreateSequenceStatement(null, null);
    }


}