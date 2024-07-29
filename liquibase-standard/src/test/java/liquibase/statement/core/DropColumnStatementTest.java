package liquibase.statement.core;

public class DropColumnStatementTest extends AbstractSqStatementTest<DropColumnStatement> {

    @Override
    protected DropColumnStatement createStatementUnderTest() {
        return new DropColumnStatement(null, null, null, null);
    }
}