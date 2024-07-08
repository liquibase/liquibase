package liquibase.statement.core;

public class DropForeignKeyConstraintStatementTest extends AbstractSqStatementTest<DropForeignKeyConstraintStatement> {

    @Override
    protected DropForeignKeyConstraintStatement createStatementUnderTest() {
        return new DropForeignKeyConstraintStatement(null, null, null, null);
    }
}
