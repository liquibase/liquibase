package liquibase.statement;

public class DropUniqueConstraintStatementTest extends AbstractSqStatementTest<DropUniqueConstraintStatement> {


    protected DropUniqueConstraintStatement createStatementUnderTest() {
        return new DropUniqueConstraintStatement(null, null, null);
    }


}
