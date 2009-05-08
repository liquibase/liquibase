package liquibase.statement;

public class CreateViewStatementTest extends AbstractSqStatementTest<CreateViewStatement> {

    protected CreateViewStatement createStatementUnderTest() {
        return new CreateViewStatement(null, null, null, false);
    }

}