package liquibase.statement.core;

import liquibase.statement.core.DropTableStatement;

public class DropTableStatementTest extends AbstractSqStatementTest<DropTableStatement> {

    @Override
    protected DropTableStatement createStatementUnderTest() {
        return new DropTableStatement(null, null, true);
    }

}
