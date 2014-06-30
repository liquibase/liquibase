package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class SelectFromDatabaseChangeLogLockStatementTest extends AbstractStatementTest<SelectFromDatabaseChangeLogLockStatement> {
    @Override
    protected SelectFromDatabaseChangeLogLockStatement createObject() {
        return new SelectFromDatabaseChangeLogLockStatement();
    }
}
