package liquibase.statement.core;

public class AddColumnStatementTest extends AbstractSqStatementTest<AddColumnStatement> {

    @Override
    protected AddColumnStatement createStatementUnderTest() {
        return new AddColumnStatement((String)null, null, null, null, null, null);
    }
}