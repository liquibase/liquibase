package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DeleteStatementTest extends AbstractStatementTest<DeleteStatement> {
    @Override
    protected DeleteStatement createObject() {
        return new DeleteStatement(null, null, null);
    }
}
