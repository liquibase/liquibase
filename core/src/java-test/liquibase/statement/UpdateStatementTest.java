package liquibase.statement;

public class UpdateStatementTest extends AbstractSqStatementTest<UpdateStatement> {

    @Override
    protected UpdateStatement createStatementUnderTest() {
        return new UpdateStatement(null, null);
    }

}
