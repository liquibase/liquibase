package liquibase.statement.core;

import liquibase.statement.StoredProcedureStatement;

public class StoredProcedureStatementTest extends AbstractSqStatementTest<StoredProcedureStatement> {
    @Override
    protected StoredProcedureStatement createStatementUnderTest() {
        return new StoredProcedureStatement(null);
    }
}
