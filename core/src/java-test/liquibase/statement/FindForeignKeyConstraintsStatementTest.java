package liquibase.statement;

public class FindForeignKeyConstraintsStatementTest extends AbstractSqStatementTest<FindForeignKeyConstraintsStatement> {

    protected FindForeignKeyConstraintsStatement createStatementUnderTest() {
        return new FindForeignKeyConstraintsStatement(null, null);
    }

}
