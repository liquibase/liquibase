package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class RenameTableStatementTest extends AbstractStatementTest<RenameTableStatement> {

    @Override
    protected RenameTableStatement createObject() {
        return new RenameTableStatement(null, null, null, null);
    }


}
