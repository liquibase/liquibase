package liquibase.statement;

public class CreateTableStatementTest extends AbstractSqStatementTest<CreateTableStatement> {

    protected CreateTableStatement createStatementUnderTest() {
        return new CreateTableStatement(null, null);
    }

}
