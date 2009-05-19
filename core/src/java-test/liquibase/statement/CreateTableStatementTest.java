package liquibase.statement;

public class CreateTableStatementTest extends AbstractSqStatementTest<CreateTableStatement> {

    @Override
    protected CreateTableStatement createStatementUnderTest() {
        return new CreateTableStatement(null, null);
    }

}
