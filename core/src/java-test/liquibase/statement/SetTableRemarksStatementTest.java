package liquibase.statement;

public class SetTableRemarksStatementTest extends AbstractSqStatementTest<SetTableRemarksStatement> {
    protected SetTableRemarksStatement createStatementUnderTest() {
        return new SetTableRemarksStatement(null, null, null);
    }
}
