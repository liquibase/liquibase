package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class ReindexStatementTest extends AbstractStatementTest<ReindexStatement> {
    @Override
    protected ReindexStatement createObject() {
        return new ReindexStatement(null, null, null);
    }

//    @Override
//    public void hasAtLeastOneGenerator() {
//        //todo: remove override once SQLite is back in TestContext.AllDatabases
//    }
}
