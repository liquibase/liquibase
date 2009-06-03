package liquibase.sqlgenerator.core;

import liquibase.statement.SelectFromDatabaseChangeLogLockStatement;
import liquibase.sqlgenerator.core.SelectFromDatabaseChangeLogLockGenerator;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;

public class SelectFromDatabaseChangeLogLockGeneratorTest extends AbstractSqlGeneratorTest<SelectFromDatabaseChangeLogLockStatement> {
    public SelectFromDatabaseChangeLogLockGeneratorTest() throws Exception {
        super( new SelectFromDatabaseChangeLogLockGenerator());
    }

    @Override
    protected SelectFromDatabaseChangeLogLockStatement createSampleSqlStatement() {
        return new SelectFromDatabaseChangeLogLockStatement("LOCKED");
    }
}
