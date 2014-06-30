package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class CreateTableStatementTest extends AbstractStatementTest<CreateTableStatement> {

    @Override
    protected CreateTableStatement createObject() {
        return new CreateTableStatement(null, null, null);
    }

}
