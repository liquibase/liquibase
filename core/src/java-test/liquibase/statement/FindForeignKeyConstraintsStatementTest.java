package liquibase.statement;

public class FindForeignKeyConstraintsStatementTest extends AbstractSqStatementTest<FindForeignKeyConstraintsStatement> {

    @Override
    protected FindForeignKeyConstraintsStatement createStatementUnderTest() {
        return new FindForeignKeyConstraintsStatement(null, null);
    }

}
