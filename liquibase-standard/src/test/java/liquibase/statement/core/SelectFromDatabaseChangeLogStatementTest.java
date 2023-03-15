package liquibase.statement.core;

public class SelectFromDatabaseChangeLogStatementTest extends AbstractSqStatementTest<SelectFromDatabaseChangeLogStatement> {
    @Override
    protected SelectFromDatabaseChangeLogStatement createStatementUnderTest() {
        return new SelectFromDatabaseChangeLogStatement((String[])null);
    }
}