package liquibase.statement.core;

public class CreateSequenceStatementTest extends AbstractSqStatementTest<CreateSequenceStatement> {

    @Override
    protected CreateSequenceStatement createStatementUnderTest() {
        return new CreateSequenceStatement(null, null, null);
    }


}