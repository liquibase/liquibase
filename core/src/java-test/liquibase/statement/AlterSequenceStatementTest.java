package liquibase.statement;

public class AlterSequenceStatementTest extends AbstractSqStatementTest<AlterSequenceStatement> {

    @Override
    protected AlterSequenceStatement createStatementUnderTest() {
        return new AlterSequenceStatement(null, null);
    }


}
