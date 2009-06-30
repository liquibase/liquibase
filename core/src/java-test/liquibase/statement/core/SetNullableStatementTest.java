package liquibase.statement.core;

import liquibase.statement.core.SetNullableStatement;

public class SetNullableStatementTest extends AbstractSqStatementTest<SetNullableStatement> {

    @Override
    protected SetNullableStatement createStatementUnderTest() {
        return new SetNullableStatement(null, null, null, null, true);
    }

}
