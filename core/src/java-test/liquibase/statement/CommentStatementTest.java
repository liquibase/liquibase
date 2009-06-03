package liquibase.statement;

public class CommentStatementTest extends AbstractSqStatementTest<CommentStatement> {

    @Override
    protected CommentStatement createStatementUnderTest() {
        return new CommentStatement(null);
    }

}
