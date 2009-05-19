package liquibase.statement;

public class DropTableStatementTest extends AbstractSqStatementTest<DropTableStatement> {

    @Override
    protected DropTableStatement createStatementUnderTest() {
        return new DropTableStatement(null, null, true);
    }

}
