package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class StoredProcedureStatementTest extends AbstractStatementTest<StoredProcedureStatement> {
    @Override
    protected StoredProcedureStatement createObject() {
        return new StoredProcedureStatement(null);
    }
}
