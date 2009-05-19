package liquibase.statement;

public class SetColumnRemarksStatementTest extends AbstractSqStatementTest<SetColumnRemarksStatement> {
    @Override
    protected SetColumnRemarksStatement createStatementUnderTest() {
        return new SetColumnRemarksStatement(null, null, null, null);
    }
}
