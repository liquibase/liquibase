package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class FindForeignKeyConstraintsStatementTest extends AbstractStatementTest<FindForeignKeyConstraintsStatement> {

    @Override
    protected FindForeignKeyConstraintsStatement createObject() {
        return new FindForeignKeyConstraintsStatement(null, null, null);
    }

}
