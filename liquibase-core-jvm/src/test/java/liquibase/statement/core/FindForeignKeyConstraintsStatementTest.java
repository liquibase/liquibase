package liquibase.statement.core;

import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsStatementTest extends AbstractSqStatementTest<FindForeignKeyConstraintsStatement> {

    @Override
    protected FindForeignKeyConstraintsStatement createStatementUnderTest() {
        return new FindForeignKeyConstraintsStatement(null, null);
    }

}
