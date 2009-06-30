package liquibase.statement.core;

import liquibase.statement.core.AddUniqueConstraintStatement;

public class AddUniqueConstraintStatementTest extends AbstractSqStatementTest<AddUniqueConstraintStatement> {

    @Override
    protected AddUniqueConstraintStatement createStatementUnderTest() {
        return new AddUniqueConstraintStatement(null, null, null, null);
    }


}
