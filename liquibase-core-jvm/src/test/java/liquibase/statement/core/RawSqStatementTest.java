package liquibase.statement.core;

import liquibase.statement.core.RawSqlStatement;

public class RawSqStatementTest extends AbstractSqStatementTest<RawSqlStatement> {

    @Override
    protected RawSqlStatement createStatementUnderTest() {
        return new RawSqlStatement(null);
    }

}
