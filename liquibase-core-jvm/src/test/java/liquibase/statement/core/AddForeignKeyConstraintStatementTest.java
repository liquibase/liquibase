package liquibase.statement.core;

import liquibase.statement.core.AddForeignKeyConstraintStatement;

public class AddForeignKeyConstraintStatementTest extends AbstractSqStatementTest<AddForeignKeyConstraintStatement> {

    @Override
    protected AddForeignKeyConstraintStatement createStatementUnderTest() {
        return new AddForeignKeyConstraintStatement(null, null, null, null, null, null, null);
    }

}
