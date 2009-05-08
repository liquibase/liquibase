package liquibase.statement;

public class DropForeignKeyConstraintStatementTest extends AbstractSqStatementTest<DropForeignKeyConstraintStatement> {

    protected DropForeignKeyConstraintStatement createStatementUnderTest() {
        return new DropForeignKeyConstraintStatement(null, null, null);
    }
}
