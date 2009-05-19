package liquibase.statement;

public class DeleteStatementTest extends AbstractSqStatementTest<DeleteStatement>{
    @Override
    protected DeleteStatement createStatementUnderTest() {
        return new DeleteStatement(null, null);
    }
}
