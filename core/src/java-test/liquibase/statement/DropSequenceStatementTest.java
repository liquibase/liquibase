package liquibase.statement;

public class DropSequenceStatementTest extends AbstractSqStatementTest<DropSequenceStatement> {

    @Override
    protected DropSequenceStatement createStatementUnderTest() {
        return new DropSequenceStatement(null, null);
    }

}
