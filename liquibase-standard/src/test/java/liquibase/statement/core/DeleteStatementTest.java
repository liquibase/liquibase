package liquibase.statement.core;

public class DeleteStatementTest extends AbstractSqStatementTest<DeleteStatement>{
    @Override
    protected DeleteStatement createStatementUnderTest() {
        return new DeleteStatement(null, null, null);
    }
}
