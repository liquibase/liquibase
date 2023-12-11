package liquibase.statement.core;

public class DropTableStatementTest extends AbstractSqStatementTest<DropTableStatement> {

    @Override
    protected DropTableStatement createStatementUnderTest() {
        return new DropTableStatement(null, null, null, true);
    }

}
