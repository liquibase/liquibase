package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class AlterSequenceStatementTest extends AbstractStatementTest<AlterSequenceStatement> {

    @Override
    protected AlterSequenceStatement createObject() {
        return new AlterSequenceStatement(null, null, null);
    }


}
