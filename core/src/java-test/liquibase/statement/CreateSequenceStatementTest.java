package liquibase.statement;

public class CreateSequenceStatementTest extends AbstractSqStatementTest<CreateSequenceStatement> {

    protected CreateSequenceStatement createStatementUnderTest() {
        return new CreateSequenceStatement(null, null);
    }


}