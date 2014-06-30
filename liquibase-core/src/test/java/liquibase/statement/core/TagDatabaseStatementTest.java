package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class TagDatabaseStatementTest extends AbstractStatementTest<TagDatabaseStatement> {

    @Override
    protected TagDatabaseStatement createObject() {
        return new TagDatabaseStatement(null);
    }


}