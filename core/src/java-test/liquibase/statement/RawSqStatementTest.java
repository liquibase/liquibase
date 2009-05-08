package liquibase.statement;

public class RawSqStatementTest extends AbstractSqStatementTest<RawSqlStatement> {

    protected RawSqlStatement createStatementUnderTest() {
        return new RawSqlStatement(null);
    }

}
