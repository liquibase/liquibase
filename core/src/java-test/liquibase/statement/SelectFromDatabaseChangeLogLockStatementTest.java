package liquibase.statement;

public class SelectFromDatabaseChangeLogLockStatementTest extends AbstractSqStatementTest<SelectFromDatabaseChangeLogLockStatement> {
    protected SelectFromDatabaseChangeLogLockStatement createStatementUnderTest() {
        return new SelectFromDatabaseChangeLogLockStatement(null);
    }
}
