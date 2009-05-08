package liquibase.statement;

public class UpdateStatementTest extends AbstractSqStatementTest<UpdateStatement> {

    protected UpdateStatement createStatementUnderTest() {
        return new UpdateStatement(null, null);
    }

}
