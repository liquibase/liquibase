package liquibase.statement;

public class ReindexStatementTest extends AbstractSqStatementTest<ReindexStatement> {
    protected ReindexStatement createStatementUnderTest() {
        return new ReindexStatement(null, null);
    }
}
