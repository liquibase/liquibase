package liquibase.statement.core;

import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class DropForeignKeyConstraintStatementTest extends AbstractSqStatementTest<DropForeignKeyConstraintStatement> {

    @Override
    protected DropForeignKeyConstraintStatement createStatementUnderTest() {
        return new DropForeignKeyConstraintStatement(null, null, null);
    }
}
