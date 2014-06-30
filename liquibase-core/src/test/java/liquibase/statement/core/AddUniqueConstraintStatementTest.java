package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class AddUniqueConstraintStatementTest extends AbstractStatementTest<AddUniqueConstraintStatement> {

    @Override
    protected AddUniqueConstraintStatement createObject() {
        return new AddUniqueConstraintStatement(null, null, null, null, null);
    }


}
