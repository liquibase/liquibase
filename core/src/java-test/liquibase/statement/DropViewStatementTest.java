package liquibase.statement;

public class DropViewStatementTest extends AbstractSqStatementTest<DropViewStatement> {

    protected DropViewStatement createStatementUnderTest() {
        return new DropViewStatement(null, null);
    }


}
