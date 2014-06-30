package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class RenameViewStatementTest extends AbstractStatementTest<RenameViewStatement> {

    @Override
    protected RenameViewStatement createObject() {
        return new RenameViewStatement(null, null, null, null);
    }


}
