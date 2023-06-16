package liquibase.statement.core;

public class SelectFromDatabaseChangeLogLockStatementTest extends AbstractSqStatementTest<SelectFromDatabaseChangeLogLockStatement> {
    @Override
    protected SelectFromDatabaseChangeLogLockStatement createStatementUnderTest() {
        return new SelectFromDatabaseChangeLogLockStatement();
    }
}
