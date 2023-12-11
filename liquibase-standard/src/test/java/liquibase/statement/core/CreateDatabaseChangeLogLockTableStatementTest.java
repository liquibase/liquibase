package liquibase.statement.core;

public class CreateDatabaseChangeLogLockTableStatementTest extends AbstractSqStatementTest<CreateDatabaseChangeLogLockTableStatement> {
    @Override
    protected CreateDatabaseChangeLogLockTableStatement createStatementUnderTest() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
}
