package liquibase.statement.core;

import liquibase.statement.core.DropUniqueConstraintStatement;

public class DropUniqueConstraintStatementTest extends AbstractSqStatementTest<DropUniqueConstraintStatement> {


    @Override
    protected DropUniqueConstraintStatement createStatementUnderTest() {
        return new DropUniqueConstraintStatement(null, null, null);
    }


}
