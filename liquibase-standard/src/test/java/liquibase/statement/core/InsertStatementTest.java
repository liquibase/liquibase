package liquibase.statement.core;

public class InsertStatementTest extends AbstractSqStatementTest<InsertStatement> {

    @Override
    protected InsertStatement createStatementUnderTest() {
        return new InsertStatement(null, null, null);
    }
}
