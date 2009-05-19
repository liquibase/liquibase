package liquibase.statement;

public class DropPrimaryKeyStatementTest extends AbstractSqStatementTest<DropPrimaryKeyStatement> {

    @Override
    protected DropPrimaryKeyStatement createStatementUnderTest() {
        return new DropPrimaryKeyStatement(null, null, null);
    }

}
