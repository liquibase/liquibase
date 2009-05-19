package liquibase.statement;

public class ReindexStatementTest extends AbstractSqStatementTest<ReindexStatement> {
    @Override
    protected ReindexStatement createStatementUnderTest() {
        return new ReindexStatement(null, null);
    }
}
