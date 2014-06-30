package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class UpdateStatementTest extends AbstractStatementTest<UpdateStatement> {

    @Override
    protected UpdateStatement createObject() {
        return new UpdateStatement(null, null, null);
    }

}
