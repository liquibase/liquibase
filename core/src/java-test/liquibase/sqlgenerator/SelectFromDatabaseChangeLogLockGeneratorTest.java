package liquibase.sqlgenerator;

import liquibase.statement.SelectFromDatabaseChangeLogLockStatement;

public class SelectFromDatabaseChangeLogLockGeneratorTest extends AbstractSqlGeneratorTest<SelectFromDatabaseChangeLogLockStatement> {
    public SelectFromDatabaseChangeLogLockGeneratorTest() throws Exception {
        super( new SelectFromDatabaseChangeLogLockGenerator());
    }

    @Override
    protected SelectFromDatabaseChangeLogLockStatement createSampleSqlStatement() {
        return new SelectFromDatabaseChangeLogLockStatement("LOCKED");
    }
}
