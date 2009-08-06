package liquibase.statement.core;

import liquibase.statement.core.ReorganizeTableStatement;

public class ReorganizeTableStatementTest extends AbstractSqStatementTest<ReorganizeTableStatement> {

    @Override
    protected ReorganizeTableStatement createStatementUnderTest() {
        return new ReorganizeTableStatement(null, null);
    }

}
