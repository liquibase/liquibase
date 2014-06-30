package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class CommentStatementTest extends AbstractStatementTest<CommentStatement> {

    @Override
    protected CommentStatement createObject() {
        return new CommentStatement(null);
    }

}
