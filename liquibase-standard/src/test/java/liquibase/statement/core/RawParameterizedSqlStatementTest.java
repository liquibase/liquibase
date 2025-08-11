package liquibase.statement.core;

public class RawParameterizedSqlStatementTest extends AbstractSqStatementTest<RawParameterizedSqlStatement> {

    @Override
    protected RawParameterizedSqlStatement createStatementUnderTest() {
        return new RawParameterizedSqlStatement(null);
    }

}
