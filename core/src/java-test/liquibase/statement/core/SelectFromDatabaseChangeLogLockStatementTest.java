package liquibase.statement.core;

import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;

public class SelectFromDatabaseChangeLogLockStatementTest extends AbstractSqStatementTest<SelectFromDatabaseChangeLogLockStatement> {
    @Override
    protected SelectFromDatabaseChangeLogLockStatement createStatementUnderTest() {
        return new SelectFromDatabaseChangeLogLockStatement(null);
    }
}
