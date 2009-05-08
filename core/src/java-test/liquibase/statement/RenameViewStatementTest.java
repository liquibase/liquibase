package liquibase.statement;

public class RenameViewStatementTest extends AbstractSqStatementTest<RenameViewStatement> {

    protected RenameViewStatement createStatementUnderTest() {
        return new RenameViewStatement(null, null, null);
    }


}
