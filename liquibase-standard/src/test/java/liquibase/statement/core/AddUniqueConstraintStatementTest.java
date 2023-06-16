package liquibase.statement.core;

public class AddUniqueConstraintStatementTest extends AbstractSqStatementTest<AddUniqueConstraintStatement> {

    @Override
    protected AddUniqueConstraintStatement createStatementUnderTest() {
        return new AddUniqueConstraintStatement(null, null, null, null, null);
    }


}
