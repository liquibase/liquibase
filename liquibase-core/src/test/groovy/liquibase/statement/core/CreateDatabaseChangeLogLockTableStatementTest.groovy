package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class CreateDatabaseChangeLogLockTableStatementTest extends AbstractStatementTest {

    @Override
    protected List<String> getStandardProperties() {
        return ["NONE"]
    }
}
