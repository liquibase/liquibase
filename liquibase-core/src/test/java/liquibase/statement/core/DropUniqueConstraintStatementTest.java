package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropUniqueConstraintStatementTest extends AbstractStatementTest<DropUniqueConstraintStatement> {


    @Override
    protected DropUniqueConstraintStatement createObject() {
        return new DropUniqueConstraintStatement(null, null, null, null);
    }


}
