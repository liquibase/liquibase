package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class RawSqStatementTest extends AbstractStatementTest<RawSqlStatement> {

    @Override
    protected RawSqlStatement createObject() {
        return new RawSqlStatement(null);
    }

}
