package liquibase.statement;

public class DropTableStatementTest extends AbstractSqStatementTest<DropTableStatement> {

    protected DropTableStatement createStatementUnderTest() {
        return new DropTableStatement(null, null, true);
    }

}
