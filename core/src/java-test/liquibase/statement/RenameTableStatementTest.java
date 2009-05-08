package liquibase.statement;

public class RenameTableStatementTest extends AbstractSqStatementTest<RenameTableStatement> {

    protected RenameTableStatement createStatementUnderTest() {
        return new RenameTableStatement(null, null, null);
    }


}
