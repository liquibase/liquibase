package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class SelectFromDatabaseChangeLogStatementTest extends AbstractStatementTest<SelectFromDatabaseChangeLogStatement> {
    @Override
    protected SelectFromDatabaseChangeLogStatement createObject() {
        return new SelectFromDatabaseChangeLogStatement((String[])null);
    }
}