package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class CreateDatabaseChangeLogLockTableStatementTest extends AbstractStatementTest<CreateDatabaseChangeLogLockTableStatement> {
    @Override
    protected CreateDatabaseChangeLogLockTableStatement createObject() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
}
