package liquibase.statement.core;

import liquibase.statement.core.AlterSequenceStatement;

public class AlterSequenceStatementTest extends AbstractSqStatementTest<AlterSequenceStatement> {

    @Override
    protected AlterSequenceStatement createStatementUnderTest() {
        return new AlterSequenceStatement(null, null);
    }


}
