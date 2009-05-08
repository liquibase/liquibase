package liquibase.statement;

public class CreateIndexStatementTest extends AbstractSqStatementTest<SqlStatement> {

    protected SqlStatement createStatementUnderTest() {
        return new CreateIndexStatement(null, null, null, null);
    }

}
