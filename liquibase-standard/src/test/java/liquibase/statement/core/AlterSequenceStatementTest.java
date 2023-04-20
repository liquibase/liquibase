package liquibase.statement.core;

public class AlterSequenceStatementTest extends AbstractSqStatementTest<AlterSequenceStatement> {

    @Override
    protected AlterSequenceStatement createStatementUnderTest() {
        return new AlterSequenceStatement(null, null, null);
    }


}
