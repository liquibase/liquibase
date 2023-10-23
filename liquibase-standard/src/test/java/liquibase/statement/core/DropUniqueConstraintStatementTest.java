package liquibase.statement.core;

public class DropUniqueConstraintStatementTest extends AbstractSqStatementTest<DropUniqueConstraintStatement> {


    @Override
    protected DropUniqueConstraintStatement createStatementUnderTest() {
        return new DropUniqueConstraintStatement(null, null, null, null);
    }


}
