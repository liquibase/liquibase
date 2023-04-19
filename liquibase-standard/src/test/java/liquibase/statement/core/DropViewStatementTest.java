package liquibase.statement.core;

public class DropViewStatementTest extends AbstractSqStatementTest<DropViewStatement> {

    @Override
    protected DropViewStatement createStatementUnderTest() {
        return new DropViewStatement(null, null, null);
    }


}
