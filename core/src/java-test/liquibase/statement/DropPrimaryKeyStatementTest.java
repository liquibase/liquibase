package liquibase.statement;

public class DropPrimaryKeyStatementTest extends AbstractSqStatementTest<DropPrimaryKeyStatement> {

    protected DropPrimaryKeyStatement createStatementUnderTest() {
        return new DropPrimaryKeyStatement(null, null, null);
    }

}
