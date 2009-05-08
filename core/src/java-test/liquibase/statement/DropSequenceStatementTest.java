package liquibase.statement;

public class DropSequenceStatementTest extends AbstractSqStatementTest<DropSequenceStatement> {

    protected DropSequenceStatement createStatementUnderTest() {
        return new DropSequenceStatement(null, null);
    }

}
