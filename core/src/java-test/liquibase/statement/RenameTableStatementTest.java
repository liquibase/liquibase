package liquibase.statement;

public class RenameTableStatementTest extends AbstractSqStatementTest<RenameTableStatement> {

    @Override
    protected RenameTableStatement createStatementUnderTest() {
        return new RenameTableStatement(null, null, null);
    }


}
