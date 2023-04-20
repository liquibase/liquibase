package liquibase.statement.core;

public class RawSqStatementTest extends AbstractSqStatementTest<RawSqlStatement> {

    @Override
    protected RawSqlStatement createStatementUnderTest() {
        return new RawSqlStatement(null);
    }

}
