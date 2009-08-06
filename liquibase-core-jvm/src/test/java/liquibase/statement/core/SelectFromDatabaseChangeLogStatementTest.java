package liquibase.statement.core;

import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public class SelectFromDatabaseChangeLogStatementTest extends AbstractSqStatementTest<SelectFromDatabaseChangeLogStatement> {
    @Override
    protected SelectFromDatabaseChangeLogStatement createStatementUnderTest() {
        return new SelectFromDatabaseChangeLogStatement(null);
    }
}