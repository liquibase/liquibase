package liquibase.statement;

public class CreateDatabaseChangeLogLockTableStatementTest extends AbstractSqStatementTest<CreateDatabaseChangeLogLockTableStatement> {
    protected CreateDatabaseChangeLogLockTableStatement createStatementUnderTest() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
}
