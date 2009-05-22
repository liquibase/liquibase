package liquibase.statement;

public class SelectFromDatabaseChangeLogStatementTest extends AbstractSqStatementTest<SelectFromDatabaseChangeLogStatement> {
    @Override
    protected SelectFromDatabaseChangeLogStatement createStatementUnderTest() {
        return new SelectFromDatabaseChangeLogStatement(null);
    }
}