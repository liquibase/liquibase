package liquibase.statement.core;

import liquibase.statement.core.CommentStatement;

public class CommentStatementTest extends AbstractSqStatementTest<CommentStatement> {

    @Override
    protected CommentStatement createStatementUnderTest() {
        return new CommentStatement(null);
    }

}
