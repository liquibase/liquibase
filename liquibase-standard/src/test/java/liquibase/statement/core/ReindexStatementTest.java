package liquibase.statement.core;

public class ReindexStatementTest extends AbstractSqStatementTest<ReindexStatement> {
    @Override
    protected ReindexStatement createStatementUnderTest() {
        return new ReindexStatement(null, null, null);
    }

    @Override
    public void hasAtLeastOneGenerator() {
        //todo: remove override once SQLite is back in TestContext.AllDatabases
    }
}
