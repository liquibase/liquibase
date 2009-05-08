package liquibase.statement;

public class DropIndexStatementTest extends AbstractSqStatementTest<DropIndexStatement> {

    protected DropIndexStatement createStatementUnderTest() {
        return new DropIndexStatement(null, null, null);
    }

}
