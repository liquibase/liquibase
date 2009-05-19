package liquibase.statement;

public class CreateSequenceStatementTest extends AbstractSqStatementTest<CreateSequenceStatement> {

    @Override
    protected CreateSequenceStatement createStatementUnderTest() {
        return new CreateSequenceStatement(null, null);
    }


}