package liquibase.statement.core;

public class RawSqlStatementTest extends AbstractSqStatementTest<RawSqlStatement> {

    @Override
    protected RawSqlStatement createStatementUnderTest() {
        return new RawSqlStatement(null);
    }

}
