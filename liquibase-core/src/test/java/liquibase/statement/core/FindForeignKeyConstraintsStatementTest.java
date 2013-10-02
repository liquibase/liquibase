package liquibase.statement.core;

public class FindForeignKeyConstraintsStatementTest extends AbstractSqStatementTest<FindForeignKeyConstraintsStatement> {

    @Override
    protected FindForeignKeyConstraintsStatement createStatementUnderTest() {
        return new FindForeignKeyConstraintsStatement(null, null, null);
    }

}
