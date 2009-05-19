package liquibase.statement;

public class CreateIndexStatementTest extends AbstractSqStatementTest<SqlStatement> {

    @Override
    protected SqlStatement createStatementUnderTest() {
        return new CreateIndexStatement(null, null, null, null);
    }

}
