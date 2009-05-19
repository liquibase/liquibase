package liquibase.statement;

public class DropViewStatementTest extends AbstractSqStatementTest<DropViewStatement> {

    @Override
    protected DropViewStatement createStatementUnderTest() {
        return new DropViewStatement(null, null);
    }


}
