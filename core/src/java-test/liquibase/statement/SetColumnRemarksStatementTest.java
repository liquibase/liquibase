package liquibase.statement;

public class SetColumnRemarksStatementTest extends AbstractSqStatementTest<SetColumnRemarksStatement> {
    protected SetColumnRemarksStatement createStatementUnderTest() {
        return new SetColumnRemarksStatement(null, null, null, null);
    }
}
