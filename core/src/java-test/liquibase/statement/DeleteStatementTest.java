package liquibase.statement;

public class DeleteStatementTest extends AbstractSqStatementTest<DeleteStatement>{
    protected DeleteStatement createStatementUnderTest() {
        return new DeleteStatement(null, null);
    }
}
