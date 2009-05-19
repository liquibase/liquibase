package liquibase.statement;

public class DropIndexStatementTest extends AbstractSqStatementTest<DropIndexStatement> {

    @Override
    protected DropIndexStatement createStatementUnderTest() {
        return new DropIndexStatement(null, null, null);
    }

}
