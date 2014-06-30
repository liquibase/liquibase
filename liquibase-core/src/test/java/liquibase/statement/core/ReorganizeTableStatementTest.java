package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class ReorganizeTableStatementTest extends AbstractStatementTest<ReorganizeTableStatement> {

    @Override
    protected ReorganizeTableStatement createObject() {
        return new ReorganizeTableStatement(null, null, null);
    }

}
