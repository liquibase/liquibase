package liquibase.statement;

public class CreateDatabaseChangeLogLockTableStatementTest extends AbstractSqStatementTest<CreateDatabaseChangeLogLockTableStatement> {
    @Override
    protected CreateDatabaseChangeLogLockTableStatement createStatementUnderTest() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
}
