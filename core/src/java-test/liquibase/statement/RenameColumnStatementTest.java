package liquibase.statement;

public class RenameColumnStatementTest extends AbstractSqStatementTest<RenameColumnStatement> {

    protected RenameColumnStatement createStatementUnderTest() {
        return new RenameColumnStatement(null, null, null, null, null);
    }


}
