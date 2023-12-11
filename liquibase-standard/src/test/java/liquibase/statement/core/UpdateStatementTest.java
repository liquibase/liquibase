package liquibase.statement.core;

public class UpdateStatementTest extends AbstractSqStatementTest<UpdateStatement> {

    @Override
    protected UpdateStatement createStatementUnderTest() {
        return new UpdateStatement(null, null, null);
    }

}
