package liquibase.statement.core;

import liquibase.statement.core.TagDatabaseStatement;

public class TagDatabaseStatementTest extends AbstractSqStatementTest<TagDatabaseStatement> {

    @Override
    protected TagDatabaseStatement createStatementUnderTest() {
        return new TagDatabaseStatement(null);
    }


}