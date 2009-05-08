package liquibase.statement;

public class AddUniqueConstraintStatementTest extends AbstractSqStatementTest<AddUniqueConstraintStatement> {

    protected AddUniqueConstraintStatement createStatementUnderTest() {
        return new AddUniqueConstraintStatement(null, null, null, null);
    }


}
