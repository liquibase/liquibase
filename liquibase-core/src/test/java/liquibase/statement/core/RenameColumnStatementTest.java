package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class RenameColumnStatementTest extends AbstractStatementTest<RenameColumnStatement> {

    @Override
    protected RenameColumnStatement createObject() {
        return new RenameColumnStatement(null, null, null, null, null, null);
    }


}
