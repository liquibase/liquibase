package liquibase.statement;

public class AddColumnStatementTest extends AbstractSqStatementTest<AddColumnStatement> {

    protected AddColumnStatement createStatementUnderTest() {
        return new AddColumnStatement(null, null, null, null, null);
    }
}