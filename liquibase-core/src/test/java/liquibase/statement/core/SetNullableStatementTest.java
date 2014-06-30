package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class SetNullableStatementTest extends AbstractStatementTest<SetNullableStatement> {

    @Override
    protected SetNullableStatement createObject() {
        return new SetNullableStatement(null, null, null, null, null, true);
    }

}
