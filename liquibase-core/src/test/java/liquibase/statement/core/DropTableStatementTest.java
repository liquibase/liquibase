package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropTableStatementTest extends AbstractStatementTest<DropTableStatement> {

    @Override
    protected DropTableStatement createObject() {
        return new DropTableStatement(null, null, null, true);
    }

}
