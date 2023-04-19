package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class CreateIndexStatementTest extends AbstractSqStatementTest<SqlStatement> {

    @Override
    protected SqlStatement createStatementUnderTest() {
        return new CreateIndexStatement(null, null, null, null, null, null);
    }

}
