package liquibase.statement.core;

public class DropPrimaryKeyStatementTest extends AbstractSqStatementTest<DropPrimaryKeyStatement> {

    @Override
    protected DropPrimaryKeyStatement createStatementUnderTest() {
        return new DropPrimaryKeyStatement(null, null, null, null);
    }

}
