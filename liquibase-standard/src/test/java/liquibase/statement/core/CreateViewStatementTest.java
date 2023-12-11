package liquibase.statement.core;

public class CreateViewStatementTest extends AbstractSqStatementTest<CreateViewStatement> {

    @Override
    protected CreateViewStatement createStatementUnderTest() {
        return new CreateViewStatement(null, null, null, null, false);
    }

}