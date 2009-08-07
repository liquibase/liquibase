package liquibase.statement.core;

import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;

public class CreateDatabaseChangeLogLockTableStatementTest extends AbstractSqStatementTest<CreateDatabaseChangeLogLockTableStatement> {
    @Override
    protected CreateDatabaseChangeLogLockTableStatement createStatementUnderTest() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
}
