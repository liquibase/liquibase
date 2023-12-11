package liquibase.statement.core;

public class RenameViewStatementTest extends AbstractSqStatementTest<RenameViewStatement> {

    @Override
    protected RenameViewStatement createStatementUnderTest() {
        return new RenameViewStatement(null, null, null, null);
    }


}
