package liquibase.sqlgenerator.core;

import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;

public class SelectFromDatabaseChangeLogLockGeneratorTest extends AbstractSqlGeneratorTest<SelectFromDatabaseChangeLogLockStatement> {
    public SelectFromDatabaseChangeLogLockGeneratorTest() throws Exception {
        super( new SelectFromDatabaseChangeLogLockGenerator());
    }

    @Override
    protected SelectFromDatabaseChangeLogLockStatement createSampleSqlStatement() {
        return new SelectFromDatabaseChangeLogLockStatement("LOCKED");
    }
}
