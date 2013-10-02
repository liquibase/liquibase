package liquibase.statement.core;

public class SetColumnRemarksStatementTest extends AbstractSqStatementTest<SetColumnRemarksStatement> {
    @Override
    protected SetColumnRemarksStatement createStatementUnderTest() {
        return new SetColumnRemarksStatement(null, null, null, null, null);
    }
}
